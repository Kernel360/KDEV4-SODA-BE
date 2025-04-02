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

        var devCompany = companyService.getCompanyByIdByBaki(request.getDevCompanyId());
        var clientCompany = companyService.getCompanyByIdByBaki(request.getClientCompanyId());

        var devManagers = memberService.findByIds(request.getDevManagers());
        var devMembers = memberService.findByIds(request.getDevMembers());
        var clientManagers = memberService.findByIds(request.getClientManagers());
        var clientMembers = memberService.findByIds(request.getClientMembers());

        Project project = Project.create(request, devCompany, clientCompany, devManagers, devMembers, clientManagers, clientMembers);

        // 4. response DTO 생성
        return createProjectCreateResponse(project);
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
