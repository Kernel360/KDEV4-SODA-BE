package com.soda.project.service;

import com.querydsl.core.Tuple;
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
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private final StageService stageService;

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
        // 고객사 및 멤버 지정 (수정된 로직 사용)
        assignCompaniesAndMembersFromAssignments(
                request.getClientAssignments(),
                project,
                CompanyProjectRole.CLIENT_COMPANY
        );

        // 초기 stage 생성
        stageService.createInitialStages(project, request.getStageNames());

        log.info("프로젝트 생성 완료: 프로젝트 ID = {}", project.getId());

        // response 생성
        return createProjectCreateResponse(project);
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
        // 개발사 및 멤버 지정 (수정된 로직 사용)
        assignCompaniesAndMembersFromAssignments(
                request.getDevAssignments(),
                project,
                CompanyProjectRole.DEV_COMPANY
        );

        log.info("개발사 지정 완료: 프로젝트 ID = {}", projectId);

        // CONTRACT > IN_PROGRESS 로 상태 변경
        if (project.getStatus() == ProjectStatus.CONTRACT) {
            log.info("프로젝트 상태 변경 시도: ID={}, 현재 상태={}, 변경 상태={}",
                    projectId, project.getStatus(), ProjectStatus.IN_PROGRESS);
            project.changeStatus(ProjectStatus.IN_PROGRESS);
        } else {
            log.info("프로젝트 상태 변경 건너뜀: ID={}, 현재 상태={}", projectId, project.getStatus());
        }

        log.info("개발사 지정 전체 프로세스 완료: 프로젝트 ID = {}", projectId);

        // response 생성
        return createDevCompanyAssignmentResponse(project);
    }

    /**
     * 전체 프로젝트 목록 조회하는 메서드
     * 프로젝트 상태에 따라 필터링해서 반환할 수 있음
     * @return ProjectListResponse 형식으로 프로젝트 목록 반환
     */
    public Page<ProjectListResponse> getAllProjects(ProjectSearchCondition projectSearchCondition, Pageable pageable) {

        Page<ProjectListResponse> projectList = projectRepository.searchProjects(projectSearchCondition, pageable);

        log.info("프로젝트 검색/조회 완료: 조회된 페이지 크기 = {}, 총 프로젝트 수 = {}",
                projectList.getSize(),
                projectList.getTotalElements());

        return projectList;
    }

    /**
     * 특정 사용자가 참여한 프로젝트 목록 조회 메서드
     *
     * @param userId 조회하려는 사용자 ID
     * @param userRole 조회하려는 사용자의 역할
     * @param pageable 페이지네이션
     * @return ProjectListResponse 형식으로 참여 중 프로젝트 목록 반환
     */
    public Page<MyProjectListResponse> getMyProjects(ProjectSearchCondition projectSearchCondition, Long userId, String userRole, Pageable pageable) {
        log.info("사용자 프로젝트 조회 시작: 사용자 ID = {}, 사용자 역할 = {}", userId, userRole);

        if (!USER_ROLE.equals(userRole)) {
            log.warn("사용자 ID = {}가 USER_ROLE이 아님. 프로젝트 목록 조회 불가", userId);
            return Page.empty(pageable);
        }

        Page<Tuple> tuplePage = projectRepository.findMyProjectsData(projectSearchCondition, userId, pageable);
        if (tuplePage.isEmpty()) {
            log.info("사용자 ID {}가 참여한 프로젝트가 없습니다. 빈 페이지 반환.", userId);
        } else {
            log.debug("사용자 ID {}의 프로젝트 데이터(Tuple) 조회 완료. 변환 시작...", userId);
        }

        Page<MyProjectListResponse> responsePage = tuplePage.map(tuple -> mapTupleToMyProjectListResponse(tuple, true)); // memberRole 필수

        log.info("사용자 참여 프로젝트 조회 및 DTO 변환 완료 (서비스): 사용자 ID = {}, 조회된 프로젝트 수 = {}", userId, responsePage.getTotalElements());
        return responsePage;
    }

    public Page<MyProjectListResponse> getMyCompanyProjects(Long userId, Pageable pageable) {
        log.info("사용자 ID {}의 회사 참여 프로젝트 목록 조회 시작 (서비스, 단일 DTO 사용)", userId);

        Member member = memberService.findMemberById(userId);
        Company company = member.getCompany();

        if (company == null) {
            log.warn("사용자 ID {} 는 회사에 소속되어 있지 않습니다. 빈 목록 반환.", userId);
            return Page.empty(pageable);
        }
        Long companyId = company.getId();
        log.info("사용자 ID {}의 회사 ID {} 확인 완료. 프로젝트 데이터 조회 시작.", userId, companyId);

        Page<Tuple> tuplePage = projectRepository.findMyCompanyProjectsData(userId, companyId, pageable);
        if (tuplePage.isEmpty()) {
            log.info("회사 ID {}가 참여한 프로젝트가 없습니다. 빈 페이지 반환.", companyId);
        } else {
            log.debug("회사 ID {}의 프로젝트 데이터(Tuple) 조회 완료 (기준 사용자 ID: {}). 변환 시작...", companyId, userId);
        }

        Page<MyProjectListResponse> responsePage = tuplePage.map(tuple -> mapTupleToMyProjectListResponse(tuple, false)); // memberRole 선택적

        log.info("회사 ID {} 참여 프로젝트 목록 조회 및 DTO 변환 완료 (서비스, 단일 DTO 사용): {}개 조회됨", companyId, responsePage.getTotalElements());
        return responsePage;
    }

    private MyProjectListResponse mapTupleToMyProjectListResponse(Tuple tuple, boolean isMemberRoleRequired) {
        Project project = tuple.get(0, Project.class);
        CompanyProjectRole companyRole = tuple.get(1, CompanyProjectRole.class);
        MemberProjectRole memberRole = tuple.get(2, MemberProjectRole.class); // getMyCompanyProjects의 경우 null일 수 있음

        // 필수 데이터 누락 체크
        if (project == null || companyRole == null) {
            log.error("Tuple에서 필수 데이터 누락 (Project 또는 CompanyRole): tuple={}", tuple);
            throw new IllegalStateException("프로젝트 데이터 조회 중 오류 발생 (필수 데이터 누락)");
        }
        // memberRole 필수 여부 체크
        if (isMemberRoleRequired && memberRole == null) {
            log.error("Tuple에서 필수 데이터 누락 (MemberRole): tuple={}", tuple);
            throw new IllegalStateException("프로젝트 데이터 조회 중 오류 발생 (멤버 역할 누락)");
        }

        return MyProjectListResponse.from(project, companyRole, memberRole);
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
        log.info("개별 프로젝트 조회 요청 시작: projectId={}, userId={}, userRole={}", projectId, userId, userRole);

        // 1. 프로젝트 유효성 검사
        Project project = getValidProject(projectId);
        log.debug("프로젝트 확인 완료: projectId={}", projectId);

        // 2. 사용자 정보 조회
        Member member = memberService.findMemberById(userId);
        log.debug("사용자 확인 완료: userId={}", userId);

        // 3. 접근 권한 확인 (예외 대신 boolean 반환)
        boolean hasAccess = checkProjectAccess(member, userRole, project);

        // 4. 접근 권한이 없는 경우 null 반환
        if (!hasAccess) {
            log.warn("프로젝트 접근 권한 없음: projectId={}, userId={}", projectId, userId);
            return null; // 예외 대신 null 반환
        }

        // 5. 접근 권한이 있는 경우, 상세 정보 조회 및 응답 생성
        log.info("프로젝트 접근 권한 확인 완료. 상세 정보 생성 시작: projectId={}, userId={}", projectId, userId);
        ProjectViewResponse response = buildProjectViewResponse(project, member, userRole);
        log.info("프로젝트 상세 응답 DTO 생성 완료: projectId={}", projectId);

        return response;
    }

    private boolean checkProjectAccess(Member member, String userRole, Project project) {
        // 1. ADMIN 역할은 항상 접근 가능
        if (ADMIN_ROLE.equals(userRole)) {
            log.debug("ADMIN({}) 접근 허용: projectId={}, userId={}", userRole, project.getId(), member.getId());
            return true;
        }

        // 2. USER 역할인 경우, 프로젝트 참여 여부 확인
        if (USER_ROLE.equals(userRole)) {
            MemberProjectRole roleInProject = memberProjectService.getMemberRoleInProject(member, project);
            boolean isParticipant = (roleInProject != null);
            if (isParticipant) {
                log.debug("프로젝트 참여자({}) 접근 허용: projectId={}, userId={}, role={}", userRole, project.getId(), member.getId(), roleInProject);
            } else {
                log.debug("프로젝트 접근 불가 (미참여 USER): projectId={}, userId={}", project.getId(), member.getId());
            }
            return isParticipant; // 참여 여부(true/false) 반환
        }

        // 3. ADMIN 또는 USER 역할이 아닌 경우, 접근 불가
        log.warn("알 수 없는 사용자 역할({})로 프로젝트 접근 시도됨 (접근 불가 처리): projectId={}, userId={}", userRole, project.getId(), member.getId());
        return false;
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
     *
     * @param projectId 삭제할 프로젝트 ID
     * @throws GeneralException 프로젝트가 존재하지 않거나 이미 삭제된 경우
     */
    @LoggableEntityAction(action = "DELETE", entityClass = Project.class)
    @Transactional
    public void deleteProject(Long projectId) {
        // 프로젝트 유효성 검사
        Project project = getValidProject(projectId);

        // TODO ADMIN만 프로젝트 삭제 가능 (로그 오류 발생해서 추후 수정 가능하면 유효성 검사 추가 예정)
        //validateAdminRole(userRole);

        // 프로젝트 삭제 (연관된 memberProject, companyProject 함께 삭제)
        project.delete();
        companyProjectService.deleteCompanyProjects(project);
        memberProjectService.deleteMemberProjects(project);

        log.info("프로젝트 삭제 완료: projectId={}", projectId);
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

        List<Member> managers = managerIds.isEmpty() ? Collections.emptyList() : memberService.findByIds(managerIds);
        List<Member> members = memberIds.isEmpty() ? Collections.emptyList() : memberService.findByIds(memberIds);

        memberProjectService.assignMembersToProject(company, managers, project, targetManagerRole);
        memberProjectService.assignMembersToProject(company, members, project, targetMemberRole);

        log.info("프로젝트에 멤버 추가/업데이트 완료: projectId={}, companyId={}, managers={}, members={}",
                projectId, request.getCompanyId(), managers.size(), members.size());

        List<Member> addedManagers = memberProjectService.getMembersByRole(project, targetManagerRole);
        List<Member> addedMembers = memberProjectService.getMembersByRole(project, targetMemberRole);

        // response 생성
        return ProjectMemberAddResponse.from(
                project.getId(),
                company.getName(),
                addedManagers,
                addedMembers
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

    private void assignCompaniesAndMembersFromAssignments(List<CompanyAssignment> assignments,
                                                          Project project, CompanyProjectRole companyRole) {
        log.info("{} 역할 회사 및 구성원 지정 시작: 프로젝트 ID = {}", companyRole.getDescription(), project.getId());

        if (CollectionUtils.isEmpty(assignments)) {
            log.warn("지정할 {} 회사 정보가 없습니다. 프로젝트 ID = {}", companyRole.getDescription(), project.getId());
            return;
        }

        // 회사 역할에 따른 멤버 역할 결정
        MemberProjectRole managerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole memberRole = determineTargetMemberRole(companyRole);
        log.debug("결정된 멤버 역할: managerRole={}, memberRole={}", managerRole, memberRole);

        for (CompanyAssignment assignment : assignments) {
            Long companyId = assignment.getCompanyId();
            // 담당자 ID는 필수, 참여자 ID는 선택적
            List<Long> currentManagerIds = assignment.getManagerIds(); // NotEmpty DTO 제약조건 필요
            List<Long> currentMemberIds = assignment.getMemberIds() != null ? assignment.getMemberIds() : Collections.emptyList();

            if (companyId == null) {
                log.warn("회사 ID가 null");
                continue;
            }
            // 담당자 ID 목록 필수 확인
            if (CollectionUtils.isEmpty(currentManagerIds)) {
                log.error("회사 ID {} 에 대한 필수 담당자 ID 목록이 비어 있습니다.", companyId);
                // 회사별로 오류를 내거나, 전체를 롤백할지 정책 결정 필요
                throw new GeneralException(ProjectErrorCode.MEMBER_LIST_EMPTY);
            }

            // 회사 엔티티 조회
            Company company = companyService.getCompany(companyId);

            // 회사를 프로젝트에 연결
            companyProjectService.assignCompanyToProject(company, project, companyRole);
            log.info("회사-프로젝트 연결: companyId={}, role={}", companyId, companyRole);

            // 해당 회사의 담당자 멤버 조회 및 할당
            List<Member> managers = memberService.findByIds(currentManagerIds);
            validateMembersBelongToCompany(managers, company); // 소속 검증
            memberProjectService.assignMembersToProject(company, managers, project, managerRole);
            log.info("담당자 {}명 연결 완료: companyId={}", managers.size(), companyId);

            // 해당 회사의 참여자 멤버 조회 및 할당
            if (!currentMemberIds.isEmpty()) {
                List<Member> members = memberService.findByIds(currentMemberIds);
                validateMembersBelongToCompany(members, company); // 소속 검증
                memberProjectService.assignMembersToProject(company, members, project, memberRole);
                log.info("참여자 {}명 연결 완료: companyId={}", members.size(), companyId);
            }
            log.info("회사 ID {} 및 소속 멤버 지정 완료: 프로젝트 ID = {}", companyId, project.getId());
        }

        log.info("모든 {} 역할 회사 및 구성원 지정 완료: 프로젝트 ID = {}", companyRole.getDescription(), project.getId());
    }

    private ProjectCreateResponse createProjectCreateResponse(Project project) {
        log.info("프로젝트 생성 응답 생성 시작: 프로젝트 ID = {}", project.getId());

        // 고객사 역할에 해당하는 회사 및 멤버 정보 조회
        Map<Company, Map<MemberProjectRole, List<Member>>> clientData =
                groupCompanyMembersByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        // 조회된 데이터를 기반으로 응답 DTO 생성 (DTO의 from 메서드 활용)
        return ProjectCreateResponse.from(project, clientData);
    }

    private Map<Company, Map<MemberProjectRole, List<Member>>> groupCompanyMembersByRole(Project project, CompanyProjectRole companyRole) {
        // 1. 해당 역할의 회사 목록 조회
        List<Company> companies = companyProjectService.getCompaniesByRole(project, companyRole);

        // 2. 멤버 역할 결정
        MemberProjectRole managerRole = determineTargetManagerRole(companyRole);
        MemberProjectRole memberRole = determineTargetMemberRole(companyRole);

        // 3. 회사별로 멤버 조회 및 그룹화
        return companies.stream()
                .collect(Collectors.toMap(
                        company -> company, // Key: Company 객체
                        company -> {
                            // 해당 회사의, 해당 프로젝트의, 특정 역할의 멤버 목록 조회
                            List<Member> managers = memberProjectService.getMembersByCompanyAndRole(project, company, managerRole); // 이 메서드 구현 필요
                            List<Member> members = memberProjectService.getMembersByCompanyAndRole(project, company, memberRole);   // 이 메서드 구현 필요

                            // 역할별 멤버 맵 생성
                            return Map.of(
                                    managerRole, managers != null ? managers : List.of(),
                                    memberRole, members != null ? members : List.of()
                            );
                        }
                ));
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

    private DevCompanyAssignmentResponse createDevCompanyAssignmentResponse(Project project) {
        log.info("개발사 지정 응답 생성 시작: 프로젝트 ID = {}", project.getId());

        // 1. 개발사 역할에 해당하는 회사 및 멤버 정보 그룹화 (헬퍼 메서드 사용)
        Map<Company, Map<MemberProjectRole, List<Member>>> devAssignmentData =
                groupCompanyMembersByRole(project, CompanyProjectRole.DEV_COMPANY); // 개발사 역할 지정

        // 2. 조회된 데이터를 기반으로 응답 DTO 생성 (수정된 from 메서드 활용)
        return DevCompanyAssignmentResponse.from(devAssignmentData);
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

}
