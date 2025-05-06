package com.soda.project.application;

import com.soda.global.log.data.annotation.LoggableEntityAction;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.ProjectResponseBuilder;
import com.soda.project.application.validator.ProjectValidator;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectService;
import com.soda.project.domain.ProjectStatus;
import com.soda.project.domain.company.CompanyProject;
import com.soda.project.domain.company.CompanyProjectFactory;
import com.soda.project.domain.company.CompanyProjectRole;
import com.soda.project.domain.event.ProjectCreatedEvent;
import com.soda.project.domain.member.MemberProject;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.interfaces.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final CompanyProjectFactory companyProjectFactory;
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
}
