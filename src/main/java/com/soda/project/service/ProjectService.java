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
@Transactional
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
        // 1. 기본 정보 생성
        // 프로젝트 제목은 중복 불가능
        if (projectRepository.existsByTitle(request.getTitle())) {
            throw new GeneralException(ErrorCode.PROJECT_TITLE_DUPLICATED);
        }

        Project project = createProjectEntity(request);

        // 2. 개발사 지정
        List<Member> devManagers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevManagers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = assignCompanyAndMembers(request.getDevCompanyId(), request.getDevMembers(), project,
                CompanyProjectRole.DEV_COMPANY, MemberProjectRole.DEV_PARTICIPANT);

        // 3. 고객사 지정
        List<Member> clientManagers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientManagers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = assignCompanyAndMembers(request.getClientCompanyId(), request.getClientMembers(), project,
                CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_PARTICIPANT);

        // 4. response DTO 생성
        return createProjectCreateResponse(project, devManagers, devMembers, clientManagers, clientMembers);

    }

    private Project createProjectEntity(ProjectCreateRequest request) {
        ProjectDTO projectDTO = ProjectDTO.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Project project = projectDTO.toEntity();
        projectRepository.save(project); // 프로젝트 DB에 저장
        return project;
    }

    // 회사 및 멤버 지정 (개발사 및 고객사 모두에서 사용)
    private List<Member> assignCompanyAndMembers(Long companyId, List<Long> memberIds, Project project, CompanyProjectRole companyRole, MemberProjectRole memberRole) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new GeneralException(ErrorCode.COMPANY_NOT_FOUND));

        // 회사와 프로젝트 연결
        CompanyProjectDTO companyProjectDTO = CompanyProjectDTO.builder()
                .companyId(company.getId())
                .projectId(project.getId())
                .companyProjectRole(companyRole)
                .build();

        CompanyProject companyProject = companyProjectDTO.toEntity(company, project, companyRole);
        companyProjectRepository.save(companyProject);

        // 멤버 지정
        List<Member> members = new ArrayList<>();
        if (!memberIds.isEmpty()) {
            for (Long memberId : memberIds) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

                // 지정된 회사와 멤버가 동일한지 확인
                if (!member.getCompany().getId().equals(company.getId())) {
                    throw new GeneralException(ErrorCode.INVALID_MEMBER_COMPANY);
                }

                members.add(member);

                MemberProjectDTO memberProjectDTO = MemberProjectDTO.builder()
                        .memberId(member.getId())
                        .projectId(project.getId())
                        .memberProjectRole(memberRole)
                        .build();

                MemberProject memberProject = memberProjectDTO.toEntity(member, project, memberRole);
                memberProjectRepository.save(memberProject);
            }
        }
        return members;
    }

    // 프로젝트 생성 응답 DTO 생성
    private ProjectCreateResponse createProjectCreateResponse(Project project, List<Member> devManagers, List<Member> devParticipants,
                                                              List<Member> cliManagers, List<Member> cliParticipants) {

        List<String> devManagerNames = devManagers.stream()
                .map(Member::getName)
                .collect(Collectors.toList());

        List<String> devParticipantNames = devParticipants.stream()
                .map(Member::getName)
                .collect(Collectors.toList());

        List<String> cliManagerNames = cliManagers.stream()
                .map(Member::getName)
                .collect(Collectors.toList());

        List<String> cliParticipantNames = cliParticipants.stream()
                .map(Member::getName)
                .collect(Collectors.toList());

        return ProjectCreateResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .devCompanyName(devManagers.get(0).getCompany().getName())
                .devCompanyManagers(devManagerNames) // 개발사 관리자 리스트
                .devCompanyMembers(devParticipantNames) // 개발사 직원들
                .clientCompanyName(cliManagers.get(0).getCompany().getName())
                .clientCompanyManagers(cliManagerNames) // 고객사 관리자들
                .clientCompanyMembers(cliParticipantNames) // 고객사 직원들
                .build();
    }
}
