package com.soda.project.domain;

import com.querydsl.core.Tuple;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.infrastructure.ProjectDailyStatsRepository;
import com.soda.project.infrastructure.ProjectRepository;
import com.soda.project.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    private final ProjectProvider projectProvider;
    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;
    private final ProjectDailyStatsRepository projectDailyStatsRepository;

    /**
     * 프로젝트를 생성하는 메서드
     */
    public Project createAndStoreProject(
            String title, String description, LocalDateTime startDate, LocalDateTime endDate,
            List<Company> clientCompanies, List<Member> clientManagers, List<Member> clientMembers,
            List<String> initialStageNames) {

        log.debug("ProjectService: 프로젝트 생성 및 저장 시작");

        Project project = Project.create(
                title, description, startDate, endDate,
                clientCompanies, clientManagers, clientMembers,
                initialStageNames
        );

        Project savedProject = projectProvider.store(project);
        log.info("ProjectService: 프로젝트 저장 완료: projectId={}", savedProject.getId());
        return savedProject;
    }

    /**
     * 기존 프로젝트에 개발사 및 해당 개발사의 담당자/참여자를 지정하는 메서드
     */
    public void assignCompaniesAndMembersFromAssignments(List<CompanyAssignment> assignments,
                                                         Project project, CompanyProjectRole companyRole) {
        log.info("{} 역할 회사 및 구성원 지정 시작 (ProjectService): 프로젝트 ID = {}, assignmentCount={}",
                companyRole.getDescription(), project.getId(), assignments.size());
        if (CollectionUtils.isEmpty(assignments)) return;

        MemberProjectRole managerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole memberRole = determineTargetMemberRole(companyRole);

        for (CompanyAssignment assignment : assignments) {
            Company company = companyService.getCompany(assignment.getCompanyId());
            companyProjectService.assignCompanyToProject(company, project, companyRole);

            List<Member> managers = memberService.findByIds(assignment.getManagerIds());
            memberProjectService.assignMembersToProject(company, managers, project, managerRole);

            if (!CollectionUtils.isEmpty(assignment.getMemberIds())) {
                List<Member> members = memberService.findByIds(assignment.getMemberIds());
                memberProjectService.assignMembersToProject(company, members, project, memberRole);
            }
        }
        log.info("모든 {} 역할 회사 및 구성원 지정 완료 (ProjectService): 프로젝트 ID = {}", companyRole.getDescription(), project.getId());
    }

    /**
     * 프로젝트 상태 변경
     */
    public void changeProjectStatus(Project project, ProjectStatus newStatus) {
        log.info("프로젝트 상태 변경 시도: ID={}, 현재 상태={}, 변경 상태={}",
                project.getId(), project.getStatus(), newStatus);
        project.changeStatus(newStatus);
        projectProvider.store(project);
        log.info("프로젝트 상태 변경 완료: ID={}, 새 상태={}", project.getId(), project.getStatus());
    }

    /**
     * 프로젝트 기본 정보 수정
     */
    public void updateProjectInfo(Project project, String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("프로젝트 정보 수정 시작: projectId={}, title={}", project.getId(), title);
        project.updateProjectInfo(title, description, startDate, endDate);
        projectProvider.store(project);
        log.info("프로젝트 정보 수정 완료: projectId={}", project.getId());
    }

    /**
     * 전체 프로젝트 목록 조회하는 메서드
     */
    public Page<ProjectListResponse> getAllProjects(ProjectSearchCondition condition, Pageable pageable) {
        return projectProvider.searchProjects(condition, pageable);
    }

    /**
     * 특정 사용자가 참여한 프로젝트 목록 조회 메서드
     */
    public Page<Tuple> findMyProjectsData(ProjectSearchCondition condition, Long userId, Pageable pageable) {
        return projectProvider.findMyProjectsData(condition, userId, pageable);
    }

    /**
     * 사용자의 회사가 참여한 프로젝트 목록 조회
     */
    public Page<Tuple> findMyCompanyProjectsData(Long userId, Long companyId, Pageable pageable) {
        return projectProvider.findMyCompanyProjectsData(userId, companyId, pageable);
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

    /**
     * 프로젝트 삭제
     */
    public void deleteProject(Project project) {
        projectProvider.delete(project);
        log.info("ProjectService: 프로젝트 삭제 완료: projectId={}", project.getId());
    }

    /**
     * 프로젝트 기본 정보 수정
     *
     * @param userRole 요청한 사용자의 역할 (ADMIN만 수정 가능)
     * @param projectId 수정할 프로젝트 ID
     * @param request 수정 요청 정보 DTO
     * @return 수정한 프로젝트 정보 DTO
     * @throws GeneralException 프로젝트가 존재하지 않거나 ADMIN 아닌 경우
     */
    @Transactional
    public ProjectInfoUpdateResponse updateProjectInfo(String userRole, Long projectId, ProjectInfoUpdateRequest request) {
        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN인지 유효성 검사
        validateAdminRole(userRole);

        // 프로젝트 기본 정보 업데이트
        project.updateProjectInfo(
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate()
        );
        projectRepository.save(project);

        log.info("프로젝트 정보 수정 완료: projectId={}, title={}", projectId, request.getTitle());

        // response 생성
        return ProjectInfoUpdateResponse.from(project);
    }

    /**
     * 프로젝트에 회사 및 멤버 추가
     * @param userRole 요청한 사용자의 역할 (ADMIN만 추가 가능)
     * @param projectId 추가할 프로젝트 ID
     * @param request 추가할 회사 및 멤버 정보 DTO
     * @return 회사 및 멤버가 추가된 프로젝트 정보 DTO
     * @throws GeneralException 프로젝트가 존재하지 않거나 ADMIN 아닌 경우
     */
    @Transactional
    public ProjectCompanyAddResponse addCompanyToProject(String userRole, Long projectId, ProjectCompanyAddRequest request) {
        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN인지 유효성 검사
        validateAdminRole(userRole);

        // 요청 정보 추출
        Company company = companyService.getCompany(request.getCompanyId());
        CompanyProjectRole companyRole = request.getRole();
        List<Long> managerIds = request.getManagerIds();
        List<Long> memberIds = request.getMemberIds() != null ? request.getMemberIds() : Collections.emptyList();

        // 멤버들이 회사 소속인지 검증
        List<Member> managers = memberService.findByIds(managerIds);
        List<Member> members = memberService.findByIds(memberIds);
        validateMembersBelongToCompany(managers, company);
        validateMembersBelongToCompany(members, company);

        // 회사 할당
        companyProjectService.assignCompanyToProject(company, project, companyRole);
        log.info("회사-프로젝트 연결 완료: projectId={}, companyId={}, role={}", projectId, company.getId(), companyRole);

        // 역할 할당
        MemberProjectRole targetManagerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole targetMemberRole = determineTargetMemberRole(companyRole);
        log.debug("자동 결정된 멤버 역할: managerRole={}, memberRole={}", targetManagerRole, targetMemberRole);

        memberProjectService.assignMembersToProject(company, managers, project, targetManagerRole);
        log.info("담당자 {}명 프로젝트 연결 완료", managers.size());
        if (!members.isEmpty()) {
            memberProjectService.assignMembersToProject(company, members, project, targetMemberRole);
            log.info("참여자 {}명 프로젝트 연결 완료", members.size());
        }

        log.info("프로젝트에 회사 및 멤버 추가 완료: projectId={}, companyId={}, companyRole={}, managerCount={}, memberCount={}",
                projectId, company.getId(), companyRole, managers.size(), members.size());

        // response 생성
        return buildResponse(project, company, companyRole, managers, members);
    }

    /**
     * 프로젝트에 참여 중인 회사 삭제 메서드 (회사 삭제되면 해당 회사의 멤버도 자동 삭제)
     *
     * @param userRole 요청자의 역할 (ADMIN만 가능)
     * @param projectId 삭제할 회사가 참여 중인 프로젝트 ID
     * @param companyId 삭제할 회사 ID
     * @throws GeneralException ADMIN이 아니거나 유효한 프로젝트가 아닌 경우
     */
    @Transactional
    public void deleteCompanyFromProject(String userRole, Long projectId, Long companyId) {
        // 프로젝트 유효성 확인
        Project project = getValidProject(projectId);

        // ADMIN인지 유효성 검사
        validateAdminRole(userRole);

        // CompanyProject, MemberProject 삭제
        companyProjectService.deleteCompanyFromProject(project, companyId);
        log.info("프로젝트에서 회사 연결 삭제 완료: projectId={}, companyId={}", projectId, companyId);

        memberProjectService.deleteMembersFromProject(project, companyId);
        log.info("프로젝트에서 회사 소속 멤버 삭제 완료: projectId={}, companyId={}", projectId, companyId);
    }

    /**
     * 해당 프로젝트에 멤버를 추가하는 메서드
     *
     * @param userRole 추가하려는 사용자 역할
     * @param projectId 추가하려는 프로젝트 ID
     * @param request 추가하려는 request
     * @return add response
     */
    @Transactional
    public ProjectMemberAddResponse addMemberToProject(String userRole, Long projectId, ProjectMemberAddRequest request) {
        // 요청 유효성 검사 (추가할 멤버가 있는가)
        List<Long> managerIds = request.getManagerIds() != null ? request.getManagerIds() : Collections.emptyList();
        List<Long> memberIds = request.getMemberIds() != null ? request.getMemberIds() : Collections.emptyList();

        if (CollectionUtils.isEmpty(managerIds) && CollectionUtils.isEmpty(memberIds)) {
            log.error("추가할 담당자 또는 일반 참여자 ID 목록이 비어 있습니다.");
            throw new GeneralException(ProjectErrorCode.MEMBER_LIST_EMPTY); // 에러 코드 정의 필요
        }

        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN인지 유효성 검사
        validateAdminRole(userRole);

        // 회사 유효성 검사 및 프로젝트 참여 확인
        Company company = companyService.getCompany(request.getCompanyId());
        CompanyProjectRole companyRole = companyProjectService.getCompanyRoleInProject(company, project);

        if (companyRole == null) {
            log.error("회사가 프로젝트에 참여하고 있지 않습니다: projectId={}, companyId={}", projectId, request.getCompanyId());
            throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
        }

        // 멤버 역할 할당
        MemberProjectRole targetManagerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole targetMemberRole = determineTargetMemberRole(companyRole);

        if (!managerIds.isEmpty()) {
            List<Member> managers = memberService.findByIds(managerIds);
            validateMembersBelongToCompany(managers, company); // 회사 소속 검증
            memberProjectService.assignMembersToProject(company, managers, project, targetManagerRole);
            log.info("매니저 역할 처리 완료 (추가 또는 업데이트): {}명", managers.size());
        } else {
            log.info("요청에 매니저 ID 목록이 없습니다.");
        }

        if (!memberIds.isEmpty()) {
            List<Member> members = memberService.findByIds(memberIds);
            validateMembersBelongToCompany(members, company); // 회사 소속 검증
            memberProjectService.assignMembersToProject(company, members, project, targetMemberRole);
            log.info("일반 참여자 역할 처리 완료 (추가 또는 업데이트): {}명", members.size());
        } else {
            log.info("요청에 일반 참여자 ID 목록이 없습니다.");
        }

        log.info("프로젝트 멤버 추가/역할 업데이트 완료: projectId={}, companyId={}", projectId, request.getCompanyId());

        List<Member> finalManagers = memberProjectService.getMembersByCompanyAndRole(project, company, targetManagerRole);
        List<Member> finalMembers = memberProjectService.getMembersByCompanyAndRole(project, company, targetMemberRole);
        log.debug("최종 멤버 목록 조회 완료: Managers={}, Members={}", finalManagers.size(), finalMembers.size());

        // response 생성
        return ProjectMemberAddResponse.from(
                project.getId(),
                company.getName(),
                finalManagers,
                finalMembers
        );

    }

    /**
     * 프로젝트에서 해당 멤버를 삭제하는 메서드
     *
     * @param userRole 요청한 사용자 역할
     * @param projectId 멤버 삭제하려는 프로젝트 ID
     * @param memberId 삭제하려는 멤버 ID
     */
    @Transactional
    public void deleteMemberFromProject(String userRole, Long projectId, Long memberId) {
        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN인지 유효성 검사
        validateAdminRole(userRole);

        memberProjectService.deleteSingleMemberFromProject(project, memberId);
        log.info("프로젝트에서 단일 멤버 제거 완료: projectId={}, memberId={}", projectId, memberId);
    }

    /**
     * 특정 프로젝트의 멤버 목록을 필터링하여 조회합니다. (수정된 로직)
     *
     * @param projectId 대상 프로젝트 ID
     * @param pageable 페이징 및 정렬 정보
     * @return 필터링 및 페이징된 멤버 정보 응답 페이지
     * @throws GeneralException 프로젝트를 찾을 수 없는 경우
     */
    public Page<ProjectMemberResponse> getProjectMembers(
            Long projectId,
            ProjectMemberSearchCondition searchCondition,
            Pageable pageable) {
        // 1. 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // 2. companyRole 필터 처리
        CompanyProjectRole companyRole = searchCondition.getCompanyRole();
        List<Long> filteredCompanyIds = null;
        if (companyRole != null) {
            log.debug("CompanyRole 필터({}) 조회 시작", companyRole);
            filteredCompanyIds = companyProjectService.getCompanyIdsByProjectAndRoleAndIsDeletedFalse(project, companyRole);
            if (CollectionUtils.isEmpty(filteredCompanyIds)) {
                log.info("요청된 companyRole({})에 해당하는 삭제되지 않은 회사가 프로젝트({})에 없습니다. 빈 페이지 반환.", companyRole, projectId);
                return Page.empty(pageable);
            }
            log.debug("CompanyRole 필터 적용 (IsDeletedFalse): {} 역할의 회사 ID 목록 = {}", companyRole, filteredCompanyIds);
        }

        // 3. MemberProjectService 호출
        Long companyId = searchCondition.getCompanyId();
        MemberProjectRole memberRole = searchCondition.getMemberRole();
        Long memberId = searchCondition.getMemberId();

        log.debug("MemberProjectService 필터링 조회 시작: projectId={}, filteredCompanyIds={}, companyId={}, memberRole={}",
                projectId, filteredCompanyIds != null ? filteredCompanyIds : "N/A", companyId, memberRole);

        // 리포지토리/서비스 호출 시 DTO에서 추출한 값들 사용
        Page<MemberProject> memberProjectPage = memberProjectService.getFilteredMemberProjectsAndIsDeletedFalse(
                project.getId(),
                filteredCompanyIds,
                companyId,
                memberRole,
                memberId,
                pageable
        );
        log.debug("MemberProjectService 조회 완료: {}개의 삭제되지 않은 MemberProject 조회됨 (Total Elements)", memberProjectPage.getTotalElements());

        // 4. DTO 변환 (기존 로직 유지)
        Page<ProjectMemberResponse> responsePage = memberProjectPage.map(ProjectMemberResponse::from);
        log.info("삭제되지 않은 프로젝트 멤버 조회 완료: 반환 페이지 정보 (Number={}, Size={}, TotalElements={})",
                responsePage.getNumber(), responsePage.getSize(), responsePage.getTotalElements());

        return responsePage;
    }

    private void validateAdminRole(String userRole) {
        if (!ADMIN_ROLE.equals(userRole)) {
            log.error("권한 없음: 요청한 사용자 역할 = {}", userRole);
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }
    }

    public Project getValidProject(Long projectId) {
        return projectProvider.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> {
                    log.error("프로젝트를 찾을 수 없음: 프로젝트 ID = {}", projectId);
                    return new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND);
                });
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

    private void validateMembersBelongToCompany(List<Member> members, Company company) {
        if (CollectionUtils.isEmpty(members)) {
            return;
        }
        Long expectedCompanyId = company.getId();
        for (Member member : members) {
            if (member.getCompany() == null || !member.getCompany().getId().equals(expectedCompanyId)) {
                log.error("멤버가 예상된 회사({}) 소속이 아닙니다: memberId={}, memberCompany={}",
                        company.getName(), member.getId(), (member.getCompany() != null ? member.getCompany().getName() : "없음"));
                throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_SPECIFIED_COMPANY);
            }
        }
    }

    private ProjectCompanyAddResponse buildResponse(Project project, Company company, CompanyProjectRole companyRole,
                                                    List<Member> managers, List<Member> members) {
        // response 생성
        return ProjectCompanyAddResponse.from(
                project.getId(),
                company,
                companyRole,
                managers,
                members != null ? members : Collections.emptyList()
        );
    }

    private MemberProjectRole determineTargetMemberRole(CompanyProjectRole companyRole) {
        if (companyRole == CompanyProjectRole.DEV_COMPANY) {
            return MemberProjectRole.DEV_PARTICIPANT;
        } else if (companyRole == CompanyProjectRole.CLIENT_COMPANY) {
            return MemberProjectRole.CLI_PARTICIPANT;
        } else {
            log.error("멤버 역할을 결정할 수 없는 회사 역할입니다: {}", companyRole);
            throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
        }
    }

    private MemberProjectRole determineTargetManagerRole(CompanyProjectRole companyRole) {
        if (companyRole == CompanyProjectRole.DEV_COMPANY) {
            return MemberProjectRole.DEV_MANAGER;
        } else if (companyRole == CompanyProjectRole.CLIENT_COMPANY) {
            return MemberProjectRole.CLI_MANAGER;
        } else {
            log.error("매니저 역할을 결정할 수 없는 회사 역할입니다: {}", companyRole);
            throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
        }
    }

    public ProjectStatsResponse getProjectCreationTrend(Long userId, String userRole, ProjectStatsCondition statsRequest) {
        // ADMIN 유효성 검사
        validateAdminRole(userRole);

        // 날짜 유효성 검사
        LocalDate startDate = statsRequest.getStartDate();
        LocalDate endDate = statsRequest.getEndDate();
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            log.warn("잘못된 조회 기간: StartDate={}, EndDate={}", startDate, endDate);
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }

        // 집계 데이터 조회
        List<Tuple> statsData = projectDailyStatsRepository.findProjectCreationStats(startDate, endDate, statsRequest.getTimeUnit());
        log.debug("프로젝트 생성 통계 데이터 조회 완료: {}개의 데이터", statsData.size());

        // 조회 결과를 Map 변환
        Map<String, Long> statsMap = statsData.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, String.class),
                        tuple -> tuple.get(1, Long.class) != null ? tuple.get(1, Long.class) : 0L
                ));
        
        // 조회 기간 내 모든 날짜 생성 및 0으로 채우기
        List<ProjectStatsResponse.DataPoint> trendData = generateFullTrendData(startDate, endDate, statsRequest.getTimeUnit(), statsMap);
        log.debug("누락 기간 0 처리 및 최종 DataPoint 리스트 생성 완료. Size: {}", trendData.size());

        return ProjectStatsResponse.from(statsRequest, trendData);
    }

    private List<ProjectStatsResponse.DataPoint> generateFullTrendData(LocalDate startDate, LocalDate endDate,
                                                                       ProjectStatsCondition.TimeUnit timeUnit, Map<String, Long> statsMap) {
        List<ProjectStatsResponse.DataPoint> fullTrend = new ArrayList<>();

        // 시간 단위에 따라 DataPoint 생성
        switch (timeUnit) {
            case DAY:
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    long count = statsMap.getOrDefault(dateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                                    .date(dateKey)
                                    .count(count)
                            .build());

                    currentDate = currentDate.plusDays(1);
                }
                break;

            case WEEK:
                WeekFields weekFields = WeekFields.of(Locale.KOREA);
                LocalDate currentWeekStart = startDate.with(weekFields.dayOfWeek(), 1); // 시작일이 속한 주의 시작일 (월요일)
                LocalDate endWeekStart = endDate.with(weekFields.dayOfWeek(), 1); // 종료일이 속한 주의 시작일 (월요일)

                while (!currentWeekStart.isAfter(endWeekStart)) {

                    String repositoryDateKey = currentWeekStart.format(DateTimeFormatter.ofPattern("yyyy-ww", Locale.KOREA));
                    int year = currentWeekStart.getYear();
                    int month = currentWeekStart.getMonthValue();
                    int weekOfMonth = currentWeekStart.get(weekFields.weekOfMonth());
                    String displayDateString = String.format("%d년 %d월 %d주차", year, month, weekOfMonth);
                    long count = statsMap.getOrDefault(repositoryDateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                            .date(displayDateString)
                            .count(count)
                            .build());
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;

            case MONTH:
                YearMonth currentMonth = YearMonth.from(startDate);
                YearMonth endMonth = YearMonth.from(endDate);
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

                while (!currentMonth.isAfter(endMonth)) {
                    String dateKey = currentMonth.format(monthFormatter);
                    long count = statsMap.getOrDefault(dateKey, 0L);

                    fullTrend.add(ProjectStatsResponse.DataPoint.builder()
                            .date(dateKey)
                            .count(count)
                            .build());
                    currentMonth = currentMonth.plusMonths(1);
                }
                break;
        }

        return fullTrend;
    }

}
