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

        ProjectDTO projectDTO = ProjectDTO.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Project project = projectDTO.toEntity();
        projectRepository.save(project); // 프로젝트 DB에 저장

        // 2-1. 개발사 지정
        Company devCompany = companyRepository.findById(request.getDevCompanyId())
                .orElseThrow(() -> new GeneralException(ErrorCode.COMPANY_NOT_FOUND));

        CompanyProjectDTO devCompanyProjectDTO = CompanyProjectDTO.builder()
                .companyId(devCompany.getId())
                .projectId(project.getId())
                .companyProjectRole(CompanyProjectRole.DEV_COMPANY)
                .build();

        CompanyProject devCompanyProject = devCompanyProjectDTO.toEntity(devCompany, project);
        companyProjectRepository.save(devCompanyProject);

        // 2-2. 개발사 직원 지정
        List<Member> devMembers = new ArrayList<>();
        if (!request.getDevMembers().isEmpty()) {

            for (Long memberId : request.getDevMembers()) {
                Member devMember = memberRepository.findById(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

                // 지정한 개발사와 멤버의 회사가 일치해야 프로젝트 생성 가능
                if (!devMember.getCompany().getId().equals(devCompany.getId())) {
                    throw new GeneralException(ErrorCode.INVALID_MEMBER_COMPANY);
                }

                devMembers.add(devMember);

                MemberProjectDTO devMemberProjectDTO = MemberProjectDTO.builder()
                        .memberId(devMember.getId())
                        .projectId(project.getId())
                        .memberProjectRole(devMember.getId().equals(request.getDevMembers().get(0))
                                ? MemberProjectRole.DEV_MANAGER
                                : MemberProjectRole.DEV_PARTICIPANT)
                        .build();

                MemberProject devMemberProject = devMemberProjectDTO.toEntity(devMember, project);
                memberProjectRepository.save(devMemberProject);
            }
        }

        // 3-1. 고객사 지정
        Company clientCompany = companyRepository.findById(request.getClientCompanyId())
                .orElseThrow(() -> new GeneralException(ErrorCode.COMPANY_NOT_FOUND));

        CompanyProjectDTO clientCompanyProjectDTO = CompanyProjectDTO.builder()
                .companyId(clientCompany.getId())
                .projectId(project.getId())
                .companyProjectRole(CompanyProjectRole.CLIENT_COMPANY)
                .build();

        CompanyProject clientCompanyProject = clientCompanyProjectDTO.toEntity(clientCompany, project);
        companyProjectRepository.save(clientCompanyProject);

        // 3-2. 고객사 직원 지정
        List<Member> clientMembers = new ArrayList<>();
        if (!request.getClientMembers().isEmpty()) {

            for (Long memberId : request.getClientMembers()) {
                Member cliMember = memberRepository.findById(memberId)
                        .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

                // 지정한 고객사와 멤버의 회사가 일치해야 프로젝트 생성 가능
                if (!cliMember.getCompany().getId().equals(clientCompany.getId())) {
                    throw new GeneralException(ErrorCode.INVALID_MEMBER_COMPANY);
                }

                clientMembers.add(cliMember);

                MemberProjectDTO cliMemberProjectDTO = MemberProjectDTO.builder()
                        .memberId(cliMember.getId())
                        .projectId(project.getId())
                        .memberProjectRole(cliMember.getId().equals(request.getClientMembers().get(0))
                                ? MemberProjectRole.CLI_MANAGER
                                : MemberProjectRole.CLI_PARTICIPANT)
                        .build();

                MemberProject cliMemberProject = cliMemberProjectDTO.toEntity(cliMember, project);
                memberProjectRepository.save(cliMemberProject);

            }
        }

        // 4. response DTO 생성
        return ProjectCreateResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .clientCompanyName(clientCompany.getName())
                .clientCompanyManager(clientMembers.get(0).getName())
                .clientCompanyMembers(clientMembers.stream().skip(1).map(Member::getName).collect(Collectors.toList()))
                .devCompanyName(devCompany.getName())
                .devCompanyManager(devMembers.get(0).getName())
                .devCompanyMembers(devMembers.stream().skip(1).map(Member::getName).collect(Collectors.toList()))
                .build();
    }
}
