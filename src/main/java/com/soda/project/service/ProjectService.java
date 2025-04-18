package com.soda.project.service;

import com.soda.global.log.dataLog.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.dto.*;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.ProjectRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;

    /**
     * 프로젝트를 생성하는 메서드입니다.
     *
     * @param userRole  현재 요청한 사용자의 역할 (ADMIN만 허용)
     * @param request   프로젝트 생성 요청 정보를 담은 DTO
     * @return          생성된 프로젝트에 대한 응답 DTO
     * @throws GeneralException 사용자가 관리자 권한이 아닌 경우 또는 유효하지 않은 데이터가 입력된 경우 발생
     */
    @LoggableEntityAction(action = "CREATE", entityClass = Project.class)
    @Transactional
    public ProjectCreateResponse createProject(String userRole, ProjectCreateRequest request) {
        // 사용자 유효성 검사 관리자만 프로젝트 생성 가능
        validateAdminRole(userRole);

        // 날짜 순서 유효성 검사
        validateProjectDates(request.getStartDate(), request.getEndDate());

        // 프로젝트 기본 정보 생성
        Project project = createProjectEntity(request);

        // 고객사 지정
        assignClientCompanies(request, project);

        // TODO stage 수정 로직 추가 예정

        log.info("프로젝트 생성 완료: 프로젝트 ID = {}", project.getId());

        // response 생성
        return createProjectCreateResponse(project);
    }

    private ProjectCreateResponse createProjectCreateResponse(Project project) {
        log.info("프로젝트 응답 생성 시작: 프로젝트 ID = {}", project.getId());

        // 고객사 관련 정보를 추출하는 메서드
        List<Company> clientCompanies = companyProjectService.getCompaniesByRole(project, CompanyProjectRole.CLIENT_COMPANY);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        // 응답 DTO 생성
        return ProjectCreateResponse.from(project, clientCompanies, clientManagers, clientMembers);
    }

    private Project createProjectEntity(ProjectCreateRequest request) {
        log.info("프로젝트 엔티티 생성 시작: 제목 = {}", request.getTitle());

        // DTO 생성
        ProjectDTO projectDTO = ProjectDTO.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ProjectStatus.CONTRACT) // 고정값으로 지정
                .build();

        // DTO → Entity 변환 후 저장
        Project project = projectDTO.toEntity();
        return projectRepository.save(project);
    }

    private void validateProjectDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            log.error("잘못된 날짜 범위: 시작 날짜 = {}, 종료 날짜 = {}", startDate, endDate);
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
    }

    /**
     * 기존 프로젝트에 개발사 및 해당 개발사의 담당자/참여자를 지정하는 메서드
     *
     * @param projectId 프로젝트 ID (기존에 존재하는 프로젝트)
     * @param userRole 현재 요청자의 역할 (ADMIN만 허용)
     * @param request 개발사 및 구성원 지정 요청 정보를 담은 DTO
     * @return 개발사 이름, 담당자 목록, 일반 참여자 목록이 포함된 응답 DTO
     * @throws GeneralException 프로젝트가 존재하지 않거나, 요청자가 ADMIN 아닐 경우 예외 발생
     */
    // TODO 우선 log 안달고 진행
    @Transactional
    public DevCompanyAssignmentResponse assignDevCompany(Long projectId, String userRole, DevCompanyAssignmentRequest request) {
        log.info("개발사 지정 시작: 프로젝트 ID = {}", projectId);

        // project 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN 확인
        validateAdminRole(userRole);

        // 고객사 지정
        assignDevCompanies(request, project);

        log.info("개발사 지정 완료: 프로젝트 ID = {}", projectId);

        // response 생성
        return createDevCompanyAssignmentResponse(project);
    }

    private DevCompanyAssignmentResponse createDevCompanyAssignmentResponse(Project project) {
        log.info("개발사 지정 응답 생성 시작: 프로젝트 ID = {}", project.getId());

        List<Company> devCompanies = companyProjectService.getCompaniesByRole(project, CompanyProjectRole.DEV_COMPANY);
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);

        return DevCompanyAssignmentResponse.from(devCompanies, devManagers, devMembers);
    }


    private void assignClientCompanies(ProjectCreateRequest request, Project project) {
        log.info("고객사 지정 시작: 프로젝트 ID = {}", project.getId());

        assignCompaniesAndMembers(
                request.getClientCompanyIds(),
                request.getClientMangerIds(),
                request.getClientMemberIds(),
                project,
                CompanyProjectRole.CLIENT_COMPANY,
                MemberProjectRole.CLI_MANAGER,
                MemberProjectRole.CLI_PARTICIPANT
        );
    }

    void assignDevCompanies(DevCompanyAssignmentRequest request, Project project) {
        log.info("개발사 및 구성원 지정 시작: 프로젝트 ID = {}", project.getId());

        assignCompaniesAndMembers(
                request.getDevCompanyIds(),
                request.getDevMangerIds(),
                request.getDevMemberIds(),
                project,
                CompanyProjectRole.DEV_COMPANY,
                MemberProjectRole.DEV_MANAGER,
                MemberProjectRole.DEV_PARTICIPANT
        );
    }

    private void assignCompaniesAndMembers( List<Long> companyIds,
                                            List<Long> managerIds,
                                            List<Long> memberIds,
                                            Project project,
                                            CompanyProjectRole companyRole,
                                            MemberProjectRole managerRole,
                                            MemberProjectRole memberRole) {
        log.info("회사 및 구성원 지정 시작: 프로젝트 ID = {}", project.getId());

        List<Member> managers = memberService.findByIds(managerIds);
        List<Member> members = memberService.findByIds(memberIds);

        for (Long companyId : companyIds) {
            Company company = companyService.getCompany(companyId);
            companyProjectService.assignCompanyToProject(company, project, companyRole);
            memberProjectService.assignMembersToProject(company, managers, project, managerRole);
            memberProjectService.assignMembersToProject(company, members, project, memberRole);
        }

        log.info("회사 및 구성원 지정 완료: 프로젝트 ID = {}", project.getId());
    }

    private void validateAdminRole(String userRole) {
        if (!ADMIN_ROLE.equals(userRole)) {
            log.error("권한 없음: 요청한 사용자 역할 = {}", userRole);
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }
    }

    public Project getValidProject(Long projectId) {
        log.info("프로젝트 조회 시작: 프로젝트 ID = {}", projectId);

        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> {
                    log.error("프로젝트를 찾을 수 없음: 프로젝트 ID = {}", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });
    }

    /**
     * 전체 프로젝트 목록 조회하는 메서드
     * 프로젝트 상태에 따라 필터링해서 반환할 수 있음
     * @param status 필터링할 프로젝트 상태 (만약 null일 경우 전체 프로젝트 반환)
     * @return ProjectListResponse 형식으로 프로젝트 목록 반환
     */
    public Page<ProjectListResponse> getAllProjects(ProjectStatus status, Pageable pageable) {
        log.info("전체 프로젝트 조회 시작: 상태 = {}, 페이지 번호 = {}, 페이지 크기 = {}",
                status != null ? status : "전체",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Project> projectList;

        if (status != null) {
            projectList = projectRepository.findByStatusAndIsDeletedFalse(status, pageable);
        } else {
            projectList = projectRepository.findByIsDeletedFalse(pageable);
        }

        log.info("프로젝트 조회 완료: 조회된 페이지 크기 = {}, 총 프로젝트 수 = {}",
                projectList.getSize(),
                projectList.getTotalElements());

        return projectList.map(this::mapToProjectListResponse);
    }

    /**
     * 특정 사용자가 참여한 프로젝트 목록 조회 메서드
     *
     * @param userId 조회하려는 사용자 ID
     * @param userRole 조회하려는 사용자의 역할
     * @param pageable 페이지네이션
     * @return ProjectListResponse 형식으로 참여 중 프로젝트 목록 반환
     */
    public Page<ProjectListResponse> getMyProjects(Long userId, String userRole, Pageable pageable) {
        log.info("사용자 프로젝트 조회 시작: 사용자 ID = {}, 사용자 역할 = {}", userId, userRole);

        // 사용자 확인
        if (USER_ROLE.equals(userRole)) {
            log.info("사용자 프로젝트 조회: 사용자 ID = {}에 대한 프로젝트 목록 조회 시작", userId);

            Page<Long> projectIds = memberProjectService.getProjectIdsByUserId(userId, pageable);
            log.info("사용자 ID = {}가 참여한 프로젝트 ID 목록 조회 완료: 조회된 프로젝트 수 = {}", userId, projectIds.getSize());

            Page<Project> userProjectPage = projectRepository.findByIdIn(projectIds.getContent(), pageable);
            log.info("사용자 ID = {}에 대한 프로젝트 목록 조회 완료: 조회된 프로젝트 수 = {}", userId, userProjectPage.getSize());

            // 프로젝트 목록을 ProjectListResponse 형태로 변환 후 반환
            return userProjectPage.map(this::mapToProjectListResponse);
        }
        log.warn("사용자 ID = {}가 USER_ROLE이 아님. 프로젝트 목록 조회 불가", userId);
        return Page.empty();
    }

    private ProjectListResponse mapToProjectListResponse(Project project) {
        return ProjectListResponse.from(project);
    }

    /**
     * 개별 프로젝트 상세 정보 조회하는 메서드
     *
     * @param userId 요청을 보낸 사용자 ID
     * @param userRole 요청을 보낸 사용자의 역할
     * @param projectId 조회할 프로젝트 ID
     * @return ProjectViewResponse 프로젝트 상세 응답
     */
    public ProjectViewResponse getProject(Long userId, String userRole, Long projectId) {
        log.info("개별 프로젝트 조회 시작: projectId={}, userId={}, userRole={}", projectId, userId, userRole);
        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // 사용자 정보 조회
        Member member = memberService.findMemberById(userId);

        ProjectViewResponse response = buildProjectViewResponse(project, member, userRole);
        log.info("프로젝트 상세 응답 DTO 생성 완료: projectId={}", projectId);

        // response 생성
        return response;
    }

    private ProjectViewResponse buildProjectViewResponse(Project project, Member member, String userRole) {
        // 회사 이름 조회
        List<String> devCompanyNames = companyProjectService.getCompanyNamesByRole(project, CompanyProjectRole.DEV_COMPANY);
        List<String> clientCompanyNames = companyProjectService.getCompanyNamesByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        // 현재 사용자의 프로젝트 내 역할 조회
        String currentMemberProjectRole = determineMemberProjectRole(member, project, userRole);
        String currentCompanyProjectRole = determineCompanyProjectRole(member, project, userRole);

        // 역할별 멤버 목록 조회
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        List<String> devManagerNames = extractMemberNames(devManagers);
        List<String> devMemberNames = extractMemberNames(devMembers);
        List<String> clientManagerNames = extractMemberNames(clientManagers);
        List<String> clientMemberNames = extractMemberNames(clientMembers);

        return ProjectViewResponse.from(
                project,
                currentMemberProjectRole,
                currentCompanyProjectRole,
                clientCompanyNames,
                clientManagerNames,
                clientMemberNames,
                devCompanyNames,
                devManagerNames,
                devMemberNames
        );
    }

    // 참여자 이름 가져오기
    private List<String> extractMemberNames(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return List.of();
        }
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }

    // 현재 사용자가 재직 중인 company project role
    private String determineCompanyProjectRole(Member member, Project project, String userRole) {
        if (USER_ROLE.equals(userRole)) {
            if (member == null || member.getCompany() == null) {
                log.warn("회사 역할 결정 불가: 사용자 또는 회사 정보 없음. memberId={}, projectId={}",
                        (member != null ? member.getId() : "null"), project.getId());
                return "No Company Info";
            }
            CompanyProjectRole role = companyProjectService.getCompanyRoleInProject(member.getCompany(), project);
            return (role != null) ? role.getDescription() : "Unknown Role";
        } else if (ADMIN_ROLE.equals(userRole)) {
            return ADMIN_ROLE;
        }

        log.warn("알 수 없는 사용자 company 역할({}) 감지됨: memberId={}, projectId={}", userRole, member.getId(), project.getId());
        return "Unknown Role";
    }

    // 현재 사용자의 프로젝트 역할
    private String determineMemberProjectRole(Member member, Project project, String userRole) {
        if (USER_ROLE.equals(userRole)) {
            MemberProjectRole role = memberProjectService.getMemberRoleInProject(member, project);
            return (role != null) ? role.getDescription() : "Unknown Role";
        } else if (ADMIN_ROLE.equals(userRole)) {
            return ADMIN_ROLE;
        }

        log.warn("알 수 없는 사용자 역할({}) 감지됨: memberId={}, projectId={}", userRole, member.getId(), project.getId());
        return "Unknown Role";
    }

    /**
     * 프로젝트 상태 업데이트
     *
     * @param userId 요청한 사용자의 ID
     * @param userRole 요청한 사용자 역할 (ADMIN or USER)
     * @param projectId 상태 변경할 프로젝트 ID
     * @param request 변경할 상태 정보 DTO
     * @return 상태 변경 결과 응답 DTO
     * @throws GeneralException 프로젝트 찾을 수 없거나, 사용자 정보를 찾을 수 없거나, 권한이 없는 경우
     */
    @LoggableEntityAction(action = "UPDATE_STATUS", entityClass = Project.class)
    @Transactional
    public ProjectStatusUpdateResponse updateProjectStatus(Long userId, String userRole, Long projectId, @Valid ProjectStatusUpdateRequest request) {
        log.info("프로젝트 상태 변경 시작: projectId={}, userId={}, userRole={}, newStatus={}",
                projectId, userId, userRole, request.getStatus());

        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // 사용자 유효성 검사
        // ADMIN, 프로젝트 참여 중인 사람들만 상태 변경 가능
        Member member = memberService.findMemberById(userId);
        validateAdminOrUser(member, userRole, project);

        // 프로젝트 상태 업데이트
        project.changeStatus(request.getStatus());
        projectRepository.save(project);
        log.info("프로젝트 상태 변경 완료: projectId={}, newStatus={}", project.getId(), project.getStatus());

        // ProjectStatusUpdateResponse 생성
        return ProjectStatusUpdateResponse.from(project);
    }

    // ADMIN 또는 해당 프로젝트에 참여 중인 USER 경우 허용하는 메소드
    private void validateAdminOrUser(Member member, String userRole, Project project) {
        if (ADMIN_ROLE.equals(userRole)) {
            return;
        }

        if (USER_ROLE.equals(userRole)) {
            MemberProjectRole roleInProject = memberProjectService.getMemberRoleInProject(member, project);

            if (roleInProject != null) {
                return;
            }
        }

        // 권한이 없는 경우 오류 발생
        log.warn("프로젝트 상태 변경 권한 없음: userId={}, userRole={}, projectId={}", member.getId(), userRole, project.getId());
        throw new GeneralException(ProjectErrorCode.NO_PERMISSION_TO_UPDATE_STATUS);
    }
}
