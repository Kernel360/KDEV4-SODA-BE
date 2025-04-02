package com.soda.project.update;

import com.soda.global.response.GeneralException;
import com.soda.member.*;
import com.soda.project.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectUpdateService {
    private final ProjectRepository projectRepository;
    private final CompanyService companyService;
    private final MemberService memberService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;

    /*
        프로젝트 수정하기
        - 기본 정보 수정
        - 개발사 수정, 관리자/직원 수정
        - 고객사 수정, 관리자/직원 수정
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        // 1. 프로젝트 존재 여부 체크
        Project project = projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 2. 프로젝트 기본 정보 수정
        project.updateProject(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());

        // 3. 개발사 및 고객사 담당자들 및 참여자들 수정
        project.getCompanyProjects().stream()
                .filter(companyProject -> companyProject.getCompany().getId().equals(request.getClientCompanyId()))
                .findAny()
                .orElseThrow();

        // 4개의 역할에 대해 멤버를 추가하거나 수정
        updateCompanyAndMembers(request.getDevCompanyId(), request.getDevManagers(), request.getDevMembers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER, MemberProjectRole.DEV_PARTICIPANT);
        updateCompanyAndMembers(request.getClientCompanyId(), request.getClientManagers(), request.getClientMembers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER, MemberProjectRole.CLI_PARTICIPANT);

        return createProjectCreateResponse(project);
    }

    private void updateCompanyAndMembers(Long companyId, List<Long> managerIds, List<Long> memberIds, Project project,
                                         CompanyProjectRole companyRole, MemberProjectRole managerRole, MemberProjectRole memberRole) {
        // 1. 회사 지정
        Company company = companyService.getCompany(companyId);
        companyProjectService.assignCompanyToProject(company, project, companyRole);

        // 2. 개발사/고객사 관리자 및 참여자 지정
        List<Member> managers = memberService.findByIds(managerIds);
        List<Member> members = memberService.findByIds(memberIds);

        // 3. 관리자 및 참여자 멤버 추가/수정
        memberProjectService.addOrUpdateMembersInProject(project, managers, managerRole);
        memberProjectService.addOrUpdateMembersInProject(project, members, memberRole);
    }

    private ProjectResponse createProjectCreateResponse(Project project) {
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        return ProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .devCompanyName(devManagers.get(0).getCompany().getName())
                .devCompanyManagers(extractMemberNames(devManagers))
                .devCompanyMembers(extractMemberNames(devParticipants))
                .clientCompanyName(clientManagers.get(0).getCompany().getName())
                .clientCompanyManagers(extractMemberNames(clientManagers))
                .clientCompanyMembers(extractMemberNames(clientParticipants))
                .build();
    }


    private List<String> extractMemberNames(List<Member> members) {
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }
}
