package com.soda.project.application;

import com.querydsl.core.Tuple;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.member.domain.MemberService;
import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyService;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.ProjectStatus;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.company.CompanyProjectService;
import com.soda.project.domain.event.ProjectCreatedEvent;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.domain.stats.ProjectStatsService;
import com.soda.project.interfaces.dto.*;
import com.soda.project.interfaces.stats.ProjectStatsCondition;
import com.soda.project.interfaces.stats.ProjectStatsResponse;
import com.soda.project.interfaces.stats.ProjectStatusUpdateRequest;
import com.soda.project.interfaces.stats.ProjectStatusUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFacade {
    private final CompanyService companyService;
    private final MemberService memberService;
    private final ProjectService projectService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;
    private final ProjectStatsService projectStatsService;
    private final ProjectValidator projectValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final ProjectResponseBuilder projectResponseBuilder;

    @LoggableEntityAction(action = "CREATE", entityClass = Project.class)
    @Transactional
    public ProjectCreateResponse createProject(String userRole, ProjectCreateRequest request) {
        projectValidator.validateAdminRole(userRole);
        projectValidator.validateProjectDates(request.getStartDate(), request.getEndDate());

        List<Company> clientCompanies = new ArrayList<>();
        List<Member> clientManagers = new ArrayList<>();
        List<Member> clientMembers = new ArrayList<>();

        for (CompanyAssignment assignment : request.getClientAssignments()) {
            // 고객사 조회
            Company clientCompany = companyService.getCompany(assignment.getCompanyId());
            clientCompanies.add(clientCompany);

            // 고객사 매니저 조회 및 소속 검증
            if (!CollectionUtils.isEmpty(assignment.getManagerIds())) {
                List<Member> managers = memberService.findMembersByIdsAndCompany(assignment.getManagerIds(), clientCompany);
                clientManagers.addAll(managers);
            }
            // 고객사 멤버 조회 및 소속 검증
            if (!CollectionUtils.isEmpty(assignment.getMemberIds())) {
                List<Member> members = memberService.findMembersByIdsAndCompany(assignment.getMemberIds(), clientCompany);
                clientMembers.addAll(members);
            }
        }

        Project savedProject = projectService.createAndStoreProject(
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                clientCompanies,
                clientManagers,
                clientMembers,
                request.getStageNames() // 초기 스테이지 이름 전달
        );
        
        publishProjectCreatedEvent(savedProject);

        return ProjectCreateResponse.from(savedProject);
    }

    private void publishProjectCreatedEvent(Project project) {
        LocalDate creationDate = project.getCreatedAt().toLocalDate();
        eventPublisher.publishEvent(new ProjectCreatedEvent(creationDate));
    }

    @Transactional
    public DevCompanyAssignmentResponse assignDevCompany(Long projectId, String userRole, DevCompanyAssignmentRequest request) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);

        projectValidator.validateDevAssignments(request, project);
        Map<Long, Company> devCompanyMap = new HashMap<>();
        Map<Long, Member> devManagerMap = new HashMap<>();
        Map<Long, Member> devMemberMap = new HashMap<>();

        projectService.assignDevCompanyAndMembers(
                project,
                new ArrayList<>(devCompanyMap.values()),
                new ArrayList<>(devManagerMap.values()),
                new ArrayList<>(devMemberMap.values())
        );;

        if (project.getStatus() == ProjectStatus.CONTRACT) {
            project.changeStatus(ProjectStatus.IN_PROGRESS);
        }

        List<Long> assignedCompanyIds = request.getDevAssignments().stream()
                .map(CompanyAssignment::getCompanyId)
                .distinct()
                .collect(Collectors.toList());
        List<Company> assignedCompanies = companyService.findCompaniesByIds(assignedCompanyIds);

        return projectResponseBuilder.createDevCompanyAssignmentResponse(project, assignedCompanies);
    }

    public Page<ProjectListResponse> getAllProjects(ProjectSearchCondition request, Pageable pageable) {
        return projectService.getAllProjects(request, pageable);
    }

    public Page<MyProjectListResponse> getMyProjects(ProjectSearchCondition request, Long userId, String userRole, Pageable pageable) {
        // 1. 권한 검증
        if (!"USER".equals(userRole)) return Page.empty(pageable);
        // 2. 데이터 조회
        Page<Tuple> tuplePage = projectService.findMyProjectsData(request, userId, pageable);
        // 3. DTO 변환
        return projectResponseBuilder.createMyProjectListResponsePage(tuplePage, true);
    }

    public ProjectViewResponse getProject(Long userId, String userRole, Long projectId) {
        // 1. 엔티티 조회
        Member member = memberService.findWithProjectsById(userId);
        Project project = projectService.getValidProject(projectId);
        // 2. 접근 권한 검증
        projectValidator.validateProjectAccessPermission(member, project);
        // 3. 응답 DTO 생성
        return projectResponseBuilder.createProjectViewResponse(project, member, userRole);
    }

    public Page<MyProjectListResponse> getMyCompanyProjects(Long userId, Pageable pageable) {
        // 1. 사용자 및 회사 정보 조회
        Member member = memberService.findMemberById(userId);
        Company company = member.getCompany();
        if (company == null) return Page.empty(pageable);
        // 2. 데이터 조회
        Page<Tuple> tuplePage = projectService.findMyCompanyProjectsData(userId, company.getId(), pageable);
        // 3. DTO 변환
        return projectResponseBuilder.createMyProjectListResponsePage(tuplePage, false);
    }

    @LoggableEntityAction(action = "DELETE", entityClass = Project.class)
    @Transactional
    public void deleteProject(Long projectId, String userRole) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);
        projectService.deleteProject(project);
    }

    @LoggableEntityAction(action = "UPDATE_STATUS", entityClass = Project.class)
    @Transactional
    public ProjectStatusUpdateResponse updateProjectStatus(Long userId, Long projectId, ProjectStatusUpdateRequest request) {
        Member member = memberService.findMemberById(userId);
        Project project = projectService.getValidProject(projectId);
        projectValidator.validateProjectAccessPermission(member, project);

        projectService.changeProjectStatus(project, request.getStatus());
        return ProjectStatusUpdateResponse.from(project);
    }

    @Transactional
    public ProjectInfoUpdateResponse updateProjectInfo(String userRole, Long projectId, ProjectInfoUpdateRequest request) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);

        projectService.updateProjectInfo(project, request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
        return ProjectInfoUpdateResponse.from(project);
    }

    @Transactional
    public ProjectCompanyAddResponse addCompanyToProject(String userRole, Long projectId, ProjectCompanyAddRequest request) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);

        Company company = companyService.getCompany(request.getCompanyId());
        List<Member> managers = request.getManagerIds() != null ? memberService.findMembersByIdsAndCompany(request.getManagerIds(), company) : Collections.emptyList();
        List<Member> members = request.getMemberIds() != null ? memberService.findMembersByIdsAndCompany(request.getMemberIds(), company) : Collections.emptyList();

        projectService.addCompanyAndMembersToProject(
                project, company, request.getRole(), managers, members
        );

        return ProjectCompanyAddResponse.from(project.getId(), company, request.getRole(), managers, members);
    }

    @Transactional
    public void deleteCompanyFromProject(String userRole, Long projectId, Long companyId) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);

        companyProjectService.deleteCompanyFromProject(project, companyId);
        memberProjectService.deleteMembersFromProject(project, companyId);
    }

    @Transactional
    public void deleteMemberFromProject(String userRole, Long projectId, Long memberId) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);

        memberProjectService.deleteSingleMemberFromProject(project, memberId);
    }

    @Transactional
    public ProjectMemberAddResponse addMemberToProject(String userRole, Long projectId, ProjectMemberAddRequest request) {
        projectValidator.validateAdminRole(userRole);
        Project project = projectService.getValidProject(projectId);
        Company company = companyService.getCompany(request.getCompanyId());

        List<Member> managers = request.getManagerIds() != null ? memberService.findByIds(request.getManagerIds()) : Collections.emptyList();
        List<Member> members = request.getMemberIds() != null ? memberService.findByIds(request.getMemberIds()) : Collections.emptyList();

        CompanyProjectRole companyRole = companyProjectService.getCompanyRoleInProject(company, project);
        if (companyRole == null) {
            throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
        }

        memberProjectService.addOrUpdateProjectMembers(project, companyRole, managers, members);

        return ProjectMemberAddResponse.from(project.getId(), company.getName(), managers, members);
    }

    public Page<ProjectMemberResponse> getProjectMembers(Long projectId, ProjectMemberSearchCondition searchCondition, Pageable pageable) {
        Project project = projectService.getValidProject(projectId);
        CompanyProjectRole companyRoleFilter = searchCondition.getCompanyRole();
        List<Long> filteredCompanyIdsForQuery = null;

        if (companyRoleFilter != null) {
            List<Long> companyIdsByRole = companyProjectService.getCompanyIdsByProjectAndRoleAndIsDeletedFalse(project, companyRoleFilter);

            if (CollectionUtils.isEmpty(companyIdsByRole)) {
                return Page.empty(pageable);
            }
            filteredCompanyIdsForQuery = companyIdsByRole;
        }

        Long specificCompanyIdFilter = searchCondition.getCompanyId();
        MemberProjectRole memberRoleFilter = searchCondition.getMemberRole();
        Long memberIdFilter = searchCondition.getMemberId();
        Page<MemberProject> memberProjectPage = memberProjectService.getFilteredMemberProjectsAndIsDeletedFalse(
                project.getId(),
                filteredCompanyIdsForQuery,
                specificCompanyIdFilter,
                memberRoleFilter,
                memberIdFilter,
                pageable
        );

        return memberProjectPage.map(ProjectMemberResponse::from);
    }

    public ProjectStatsResponse getProjectCreationTrend(Long userId, String userRole, ProjectStatsCondition request) {
        projectValidator.validateAdminRole(userRole);
        projectValidator.validateStatsDateRange(request.getStartDate(), request.getEndDate());

        return projectStatsService.getProjectCreationTrend(request);
    }
}
