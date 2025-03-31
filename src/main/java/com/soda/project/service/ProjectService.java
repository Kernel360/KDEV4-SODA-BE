package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.domain.*;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class  ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final StageService stageService;

    /*
        프로젝트 생성하기
        - 기본 정보 생성
        - 개발사 지정, 관리자/직원 지정
        - 고객사 지정, 관리자/직원 지정
     */
    @Transactional
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        // 1. 프로젝트 제목 중복 체크
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new GeneralException(ProjectErrorCode.PROJECT_TITLE_DUPLICATED);
        }

        // 2. 기본 정보 생성
        Project project = createProjectEntity(request);

        // 3. 개발사 및 고객사 지정
        assignCompanyAndMembersToProject(request, project);

        stageService.createInitialStages(project.getId());

        // 4. response DTO 생성
        return createProjectCreateResponse(project);
    }

    private Project createProjectEntity(ProjectCreateRequest request) {
        // DTO 생성
        ProjectDTO projectDTO = ProjectDTO.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        // DTO -> Entity
        Project project = projectDTO.toEntity();
        projectRepository.save(project);
        return project;
    }

    private void assignCompanyAndMembersToProject(ProjectCreateRequest request, Project project) {
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
        companyProjectService.assignCompanyToProject(company, project, companyRole);
        memberProjectService.assignMembersToProject(company, members, project, memberRole);
    }

    private ProjectCreateResponse createProjectCreateResponse(Project project) {
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        return ProjectCreateResponse.builder()
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

    // project 삭제 (연관된 company_project, member_project 같이 삭제)
    @Transactional
    public void deleteProject(Long projectId) {
        // 1. 프로젝트 존재 여부 체크
        Project project = getValidProject(projectId);

        // 2. 프로젝트 상태를 삭제된 상태로 변경
        project.delete();

        // 3. company project, member project 삭제된 상태로 변경
        companyProjectService.deleteCompanyProjects(project);
        memberProjectService.deleteMemberProjects(project);
    }

    /*
        프로젝트 수정하기
        - 기본 정보 수정
        - 개발사 수정, 관리자/직원 수정
        - 고객사 수정, 관리자/직원 수정
     */
    @Transactional
    public ProjectCreateResponse updateProject(Long projectId, ProjectCreateRequest request) {
        // 1. 프로젝트 존재 여부 체크
        Project project = getValidProject(projectId);

        // 2. 프로젝트 기본 정보 수정
        project.updateProject(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
        projectRepository.save(project);  // 프로젝트 수정 사항 저장

        // 3. 개발사 및 고객사 담당자들 및 참여자들 수정
        assignCompanyAndMembersToProject(request, project);

        return createProjectCreateResponse(project);
    }

    public Project getValidProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

}