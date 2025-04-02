package com.soda.project.search;

import com.soda.global.response.GeneralException;
import com.soda.member.Member;
import com.soda.member.CompanyProjectRole;
import com.soda.member.MemberProjectRole;

import com.soda.project.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectSearchService {

    private final ProjectRepository projectRepository;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;

    // 전체 프로젝트 조회
    public List<ProjectListResponse> getAllProjects() {
        return projectRepository.findByIsDeletedFalse().stream()
                .map(this::mapToProjectListResponse)
                .collect(Collectors.toList());
    }

    private ProjectListResponse mapToProjectListResponse(Project project) {
        String devCompanyName = companyProjectService.getCompanyNameByRole(project, CompanyProjectRole.DEV_COMPANY);
        String clientCompanyName = companyProjectService.getCompanyNameByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        return ProjectListResponse.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .devCompanyName(devCompanyName)
                .clientCompanyName(clientCompanyName)
                .build();
    }

    // 개별 프로젝트 조회
    public ProjectResponse getProject(Long projectId) {
        Project project = getValidProject(projectId);
        return mapToProjectResponse(project);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        String devCompanyName = companyProjectService.getCompanyNameByRole(project, CompanyProjectRole.DEV_COMPANY);
        String clientCompanyName = companyProjectService.getCompanyNameByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        return ProjectResponse.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .devCompanyName(devCompanyName)
                .devCompanyManagers(extractMemberNames(devManagers))
                .devCompanyMembers(extractMemberNames(devParticipants))
                .clientCompanyName(clientCompanyName)
                .clientCompanyManagers(extractMemberNames(clientManagers))
                .clientCompanyMembers(extractMemberNames(clientParticipants))
                .build();
    }

    private List<String> extractMemberNames(List<Member> members) {
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }

    public Project getValidProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

}