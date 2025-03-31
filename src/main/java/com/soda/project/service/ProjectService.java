package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.domain.*;
import com.soda.project.entity.CompanyProject;
import com.soda.project.entity.MemberProject;
import com.soda.project.entity.Project;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.ProjectRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        List<Member> devManagers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevManagers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevMembers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientManagers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientMembers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_PARTICIPANT);

        stageService.createInitialStages(project.getId());

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
        Company company = companyService.getCompany(companyId);

        // 회사와 프로젝트가 연결되지 않은 경우 연결
        assignCompanyToProject(company.getId(), project.getId(), companyRole);

        // 멤버들을 프로젝트와 역할에 맞게 지정
        return assignMembersToProject(company, memberIds, project, memberRole);
    }

    private void assignCompanyToProject(Long companyId, Long projectId, CompanyProjectRole role) {
        Company company = companyService.getCompany(companyId);
        Project project = getProjectById(projectId);

        companyProjectService.assignCompanyToProject(company, project, role);
    }

    private List<Member> assignMembersToProject(Company company, List<Long> memberIds, Project project, MemberProjectRole memberRole) {
        List<Member> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            Member member = memberService.findByIdAndIsDeletedFalse(memberId);

            if (!member.getCompany().getId().equals(company.getId())) {
                throw new GeneralException(ProjectErrorCode.INVALID_MEMBER_COMPANY);
            }

            // 이미 멤버가 프로젝트에 존재하는지 확인
            if (!memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project)) {
                members.add(member);
                MemberProject memberProject = memberProjectService.createMemberProject(member, project, memberRole);
                memberProjectService.save(memberProject);  // 새로운 멤버를 프로젝트에 추가
            }
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
        CompanyProject companyProject = companyProjectService.findByProjectAndCompanyProjectRole(project, role);
        return companyProject.getCompany().getName();
    }

    // 개별 프로젝트 조회
    public ProjectResponse getProject(Long projectId) {
        Project project = getValidProject(projectId);

        String devCompanyName = getCompanyNameByRole(project, CompanyProjectRole.DEV_COMPANY);
        String clientCompanyName = getCompanyNameByRole(project, CompanyProjectRole.CLIENT_COMPANY);

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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));

        // 2. 이미 삭제된 프로젝트인지 체크
        if (project.getIsDeleted()) {
            throw new GeneralException(ProjectErrorCode.PROJECT_ALREADY_DELETED);
        }

        // 3. 프로젝트 상태를 삭제된 상태로 변경
        project.delete(); // isDeleted 값을 true로 변경

        // 4. 프로젝트와 관련된 회사 프로젝트들 삭제 처리
        List<CompanyProject> companyProjects = companyProjectService.findByProject(project);
        companyProjects.forEach(CompanyProject::delete); // isDeleted 값을 true로 설정

        // 5. 프로젝트와 관련된 멤버 프로젝트들 삭제 처리
        List<MemberProject> memberProjects = memberProjectService.findByProject(project);
        memberProjects.forEach(MemberProject::delete); // isDeleted 값을 true로 설정

        projectRepository.save(project);
        companyProjectService.saveAll(companyProjects);
        memberProjectService.saveAll(memberProjects);
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
        Project project = getProjectById(projectId);

        // 2. 프로젝트 기본 정보 수정
        project.updateProject(request.getTitle(), request.getDescription(), request.getStartDate(), request.getEndDate());
        projectRepository.save(project);  // 프로젝트 수정 사항 저장

        // 3. 개발사 및 고객사 담당자들 및 참여자들 수정
        updateCompanyProjectMembers(project, request.getDevCompanyId(), request.getDevManagers(), request.getDevMembers(), CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER, MemberProjectRole.DEV_PARTICIPANT);
        updateCompanyProjectMembers(project, request.getClientCompanyId(), request.getClientManagers(), request.getClientMembers(), CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER, MemberProjectRole.CLI_PARTICIPANT);

        // 4. response DTO 생성
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientParticipants = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        return createProjectCreateResponse(project, devManagers, devParticipants, clientManagers, clientParticipants);
    }

    private void updateCompanyProjectMembers(Project project, Long companyId, List<Long> managerIds, List<Long> participantIds, CompanyProjectRole companyRole, MemberProjectRole managerRole, MemberProjectRole participantRole) {
        Company company = companyService.getCompany(companyId);

        // 1. 회사와 프로젝트 연결 여부 확인 (이미 연결되어 있다면 새로운 저장 하지 않음)
        if (!companyProjectService.doesCompanyProjectExist(company, project)) {
            assignCompanyToProject(company.getId(), project.getId(), companyRole);  // 회사와 프로젝트 연결
        }

        // 2. 기존의 담당자 및 참여자들을 비활성화 처리 (삭제하지 않고, isDeleted=true로 설정)
        updateMemberProjects(project, managerIds, managerRole);
        updateMemberProjects(project, participantIds, participantRole);

        // 3. 새로 추가할 멤버들 저장 (기존에 없으면 추가)
        List<Member> newManagers = assignMembersToProject(company, managerIds, project, managerRole);
        List<Member> newParticipants = assignMembersToProject(company, participantIds, project, participantRole);

        // 새로 추가된 멤버들로 새로운 `MemberProject` 객체 생성
        saveNewMemberProjects(newManagers, project, managerRole);
        saveNewMemberProjects(newParticipants, project, participantRole);
    }

    private void updateMemberProjects(Project project, List<Long> memberIds, MemberProjectRole role) {
        // 기존 멤버들 찾기 (isDeleted가 true인 멤버도 포함)
        List<MemberProject> existingMemberProjects = memberProjectService.findByProjectAndRole(project, role);

        // 기존 멤버들과 요청된 멤버들을 비교
        List<Long> currentMemberIds = existingMemberProjects.stream()
                .map(mp -> mp.getMember().getId())
                .toList();

        // 기존 멤버들 중에서 삭제해야 할 멤버들을 찾기
        List<MemberProject> membersToRemove = existingMemberProjects.stream()
                .filter(mp -> !memberIds.contains(mp.getMember().getId()) && !mp.getIsDeleted()) // 삭제되지 않은 멤버들 중에서 비교
                .collect(Collectors.toList());

        // 삭제된 멤버들을 비활성화 처리 (isDeleted = true)
        membersToRemove.forEach(MemberProject::delete); // 삭제된 멤버들을 비활성화 처리

        // 삭제되지 않은 멤버들 중에서 새로 추가할 멤버들을 찾아서 isDeleted를 false로 설정
        List<MemberProject> membersToReActivate = existingMemberProjects.stream()
                .filter(mp -> memberIds.contains(mp.getMember().getId()) && mp.getIsDeleted()) // isDeleted가 true인 멤버들을 찾아서
                .collect(Collectors.toList());

        membersToReActivate.forEach(MemberProject::reActive); // 재활성화 (isDeleted = false)

        // 삭제된 멤버들을 저장
        memberProjectService.saveAll(membersToRemove);
        // 재활성화한 멤버들을 저장
        memberProjectService.saveAll(membersToReActivate);
    }

    private void saveNewMemberProjects(List<Member> members, Project project, MemberProjectRole role) {
        List<MemberProject> newMemberProjects = new ArrayList<>();

        // 새로 추가할 멤버들 중에서 isDeleted가 true인 멤버가 있으면, 재활성화하여 추가
        for (Member member : members) {
            // 기존 멤버 프로젝트 조회
            MemberProject existingMemberProject = memberProjectService.findByMemberAndProjectAndRole(member, project, role);

            if (existingMemberProject != null) {
                // 기존 멤버 프로젝트가 존재하고, isDeleted가 true인 경우 재활성화
                if (existingMemberProject.getIsDeleted()) {
                    existingMemberProject.markAsActive();
                    newMemberProjects.add(existingMemberProject);
                }
            } else {
                // 새 멤버 프로젝트는 새로 생성
                MemberProject newMemberProject = memberProjectService.createMemberProject(member, project, role);
                newMemberProjects.add(newMemberProject);
            }
        }

        // 새로 생성된 멤버 프로젝트들을 저장
        memberProjectService.saveAll(newMemberProjects);
    }

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

    public Project getValidProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }

}