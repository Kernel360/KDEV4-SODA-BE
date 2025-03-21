package com.soda.project.service;

import com.soda.global.response.ErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.repository.CompanyRepository;
import com.soda.member.repository.MemberRepository;
import com.soda.project.domain.*;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import com.soda.project.repository.CompanyProjectRepository;
import com.soda.project.repository.MemberProjectRepository;
import com.soda.project.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyProjectRepository companyProjectRepository;
    private final MemberProjectRepository memberProjectRepository;
    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;

    /*
        프로젝트 생성하기
        - 기본 정보 생성
        - 개발사 지정, 관리자/직원 지정
        - 고객사 지정, 관리자/직원 지정
     */
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        // 1. 프로젝트 제목 중복 체크
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new GeneralException(ErrorCode.PROJECT_TITLE_DUPLICATED);
        }

        // 2. 기본 정보 생성
        Project project = createProjectEntity(request);

        // 3. 개발사 및 고객사 지정
        List<Member> devManagers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevManagers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevMembers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientManagers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientMembers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_PARTICIPANT);

        // 4. response DTO 생성
        return createProjectCreateResponse(project, devManagers, devMembers, clientManagers, clientMembers);
    }

    private Project createProjectEntity(ProjectCreateRequest request) {
        Project project = ProjectDTO.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build().toEntity();

        projectRepository.save(project);
        return project;
    }

    private List<Member> assignCompanyAndMembers(Long companyId, List<Long> memberIds, Project project, CompanyProjectRole companyRole, MemberProjectRole memberRole) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new GeneralException(ErrorCode.COMPANY_NOT_FOUND));

        // 회사와 프로젝트가 연결되지 않은 경우 연결
        assignCompanyToProject(company, project, companyRole);

        // 멤버들을 프로젝트와 역할에 맞게 지정
        return assignMembersToProject(company, memberIds, project, memberRole);
    }

    private void assignCompanyToProject(Company company, Project project, CompanyProjectRole companyRole) {
        if (!companyProjectRepository.existsByCompanyAndProject(company, project)) {
            CompanyProject companyProject = CompanyProjectDTO.builder()
                    .companyId(company.getId())
                    .projectId(project.getId())
                    .companyProjectRole(companyRole)
                    .build().toEntity(company, project, companyRole);
            companyProjectRepository.save(companyProject);
        }
    }

    private List<Member> assignMembersToProject(Company company, List<Long> memberIds, Project project, MemberProjectRole memberRole) {
        List<Member> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

            if (!member.getCompany().getId().equals(company.getId())) {
                throw new GeneralException(ErrorCode.INVALID_MEMBER_COMPANY);
            }

            members.add(member);
            MemberProject memberProject = MemberProjectDTO.builder()
                    .memberId(member.getId())
                    .projectId(project.getId())
                    .memberProjectRole(memberRole)
                    .build().toEntity(member, project, memberRole);
            memberProjectRepository.save(memberProject);
        }
        return members;
    }

    // 프로젝트 생성 응답 DTO 생성
    private ProjectCreateResponse createProjectCreateResponse(Project project, List<Member> devManagers, List<Member> devParticipants,
                                                              List<Member> cliManagers, List<Member> cliParticipants) {
        return ProjectCreateResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .devCompanyName(devManagers.get(0).getCompany().getName())
                .devCompanyManagers(extractMemberNames(devManagers))
                .devCompanyMembers(extractMemberNames(devParticipants))
                .clientCompanyName(cliManagers.get(0).getCompany().getName())
                .clientCompanyManagers(extractMemberNames(cliManagers))
                .clientCompanyMembers(extractMemberNames(cliParticipants))
                .build();
    }

    private List<String> extractMemberNames(List<Member> members) {
        return members.stream()
                .map(Member::getName)
                .collect(Collectors.toList());
    }

    // 전체 프로젝트 조회
    public List<ProjectListResponse> getAllProjects() {
        List<Project> projectList = projectRepository.findByIsDeletedFalse();
        return projectList.stream()
                .map(project -> {
                    String devCompanyName = getCompanyNameByRole(project, CompanyProjectRole.DEV_COMPANY);
                    String clientCompanyName = getCompanyNameByRole(project, CompanyProjectRole.CLIENT_COMPANY);

                    return ProjectListResponse.builder()
                            .title(project.getTitle())
                            .description(project.getDescription())
                            .startDate(project.getStartDate())
                            .endDate(project.getEndDate())
                            .devCompanyName(devCompanyName)
                            .clientCompanyName(clientCompanyName)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String getCompanyNameByRole(Project project, CompanyProjectRole role) {
        CompanyProject companyProject = companyProjectRepository.findByProjectAndCompanyProjectRole(project, role)
                .orElseThrow(() -> new GeneralException(ErrorCode.COMPANY_NOT_FOUND));
        return companyProject.getCompany().getName();
    }

    // 개별 프로젝트 조회
    public ProjectResponse getProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ErrorCode.PROJECT_NOT_FOUND));

        String devCompanyName = getCompanyNameByRole(project, CompanyProjectRole.DEV_COMPANY);
        String clientCompanyName = getCompanyNameByRole(project, CompanyProjectRole.CLIENT_COMPANY);

        List<Member> devManagers = getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devParticipants = getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);

        List<Member> clientManagers = getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientParticipants = getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

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

    private List<Member> getMembersByRole(Project project, MemberProjectRole role) {
        return memberProjectRepository.findByProjectAndRole(project, role).stream()
                .map(MemberProject::getMember)
                .collect(Collectors.toList());
    }

    // project 삭제 (연관된 company_project, member_project 같이 삭제)
    @Transactional
    public void deleteProject(Long projectId) {
        // 1. 프로젝트 존재 여부 체크
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ErrorCode.PROJECT_NOT_FOUND));

        // 2. 이미 삭제된 프로젝트인지 체크
        if (project.getIsDeleted()) {
            throw new GeneralException(ErrorCode.PROJECT_ALREADY_DELETED);
        }

        // 3. 프로젝트 상태를 삭제된 상태로 변경
        project.delete(); // isDeleted 값을 true로 변경

        // 4. 프로젝트와 관련된 회사 프로젝트들 삭제 처리
        List<CompanyProject> companyProjects = companyProjectRepository.findByProject(project);
        companyProjects.forEach(CompanyProject::delete); // isDeleted 값을 true로 설정

        // 5. 프로젝트와 관련된 멤버 프로젝트들 삭제 처리
        List<MemberProject> memberProjects = memberProjectRepository.findByProject(project);
        memberProjects.forEach(MemberProject::delete); // isDeleted 값을 true로 설정

        projectRepository.save(project);
        companyProjectRepository.saveAll(companyProjects);
        memberProjectRepository.saveAll(memberProjects);
    }
}