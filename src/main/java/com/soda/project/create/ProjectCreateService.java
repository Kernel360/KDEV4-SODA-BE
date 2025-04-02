package com.soda.project.create;

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
public class ProjectCreateService {

    private final ProjectRepository projectRepository;
    private final StageCreateService stageCreateService;
    private final CompanyProjectCreateService companyProjectCreateService;
    private final MemberProjectCreateService memberProjectCreateService;
    private final MemberProjectService memberProjectService;
    private final MemberService memberService;
    private final CompanyService companyService;

    /*
        프로젝트 생성하기
        - 기본 정보 생성
        - 개발사 지정, 관리자/직원 지정
        - 고객사 지정, 관리자/직원 지정
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        // 1. 프로젝트 제목 중복 체크
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new GeneralException(ProjectErrorCode.PROJECT_TITLE_DUPLICATED);
        }

        // 2. 기본 정보 생성
        Project project = Project.create(request);

        // 3. 개발사 및 고객사 지정
        assignCompanyAndMembersToProject(request, project);

        stageCreateService.createInitialStages(project.getId());

        // 4. response DTO 생성
        return createProjectCreateResponse(project);
    }

    private void assignCompanyAndMembersToProject(ProjectRequest request, Project project) {
        List<Member> devManagers = memberService.findByIds(request.getDevManagers());
        List<Member> devMembers = memberService.findByIds(request.getDevMembers());
        List<Member> clientManagers = memberService.findByIds(request.getClientManagers());
        List<Member> clientMembers = memberService.findByIds(request.getClientMembers());

        assignCompanyAndMembers(request.getDevCompanyId(), devManagers, project, CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER);
        assignCompanyAndMembers(request.getDevCompanyId(), devMembers, project, CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_PARTICIPANT);
        assignCompanyAndMembers(request.getClientCompanyId(), clientManagers, project, CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER);
        assignCompanyAndMembers(request.getClientCompanyId(), clientMembers, project, CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_PARTICIPANT);
    }

    private void assignCompanyAndMembers(Long companyId, List<Member> members, Project project, CompanyProjectRole companyRole, MemberProjectRole memberRole) {
        Company company = companyService.getCompany(companyId);
        companyProjectCreateService.assignCompanyToProject(company, project, companyRole);
        memberProjectCreateService.assignMembersToProject(company, members, project, memberRole);
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
