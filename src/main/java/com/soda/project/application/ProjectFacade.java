package com.soda.project.application;

import com.querydsl.core.Tuple;
import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.ProjectStatus;
import com.soda.project.domain.company.CompanyProjectFactory;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.event.ProjectCreatedEvent;
import com.soda.project.interfaces.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectFacade {
    private final CompanyService companyService;
    private final MemberService memberService;
    private final ProjectService projectService;
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
        projectService.assignCompaniesAndMembersFromAssignments(
                request.getDevAssignments(),
                project,
                CompanyProjectRole.DEV_COMPANY
        );

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
}
