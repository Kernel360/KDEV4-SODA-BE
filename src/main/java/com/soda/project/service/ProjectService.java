package com.soda.project.service;

import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.dto.ProjectCommand;
import com.soda.project.dto.ProjectCreateRequest;
import com.soda.project.dto.ProjectCreateResponse;
import com.soda.project.entity.Project;
import com.soda.member.enums.CompanyProjectRole;
import com.soda.member.enums.MemberProjectRole;
import com.soda.project.enums.ProjectStatus;
import com.soda.project.error.ProjectErrorCode;
import com.soda.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectService {
    public static final String ADMIN_ROLE = "ADMIN";

    private final ProjectRepository projectRepository;
    private final MemberService memberService;
    private final CompanyService companyService;
    private final CompanyProjectService companyProjectService;
    private final MemberProjectService memberProjectService;

    /**
     * 프로젝트를 생성하는 메서드입니다.
     *
     * @param userRole  현재 요청한 사용자의 역할 (ADMIN만 허용)
     * @param request   프로젝트 생성 요청 정보를 담은 DTO
     * @return          생성된 프로젝트에 대한 응답 DTO
     * @throws GeneralException 사용자가 관리자 권한이 아닌 경우 또는 유효하지 않은 데이터가 입력된 경우 발생
     */
    @Transactional
    public ProjectCreateResponse createProject(String userRole, ProjectCreateRequest request) {
        // 사용자 유효성 검사 관리자만 프로젝트 생성 가능
        if (!ADMIN_ROLE.equals(userRole)) {
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }

        // 날짜 순서 유효성 검사
        validateProjectDates(request.getStartDate(), request.getEndDate());

        // 프로젝트 기본 정보 생성
        Project project = createProjectEntity(request);

        // 개발사 지정
        assignCompanyAndMembersToProject(request, project);

        // TODO stage 수정 로직 추가 예정

        // response 생성
        return createProjectCreateResponse(project);
    }

    private ProjectCreateResponse createProjectCreateResponse(Project project) {
        // 고객사 관련 정보를 추출하는 메서드
        List<Company> clientCompanies = companyProjectService.getClientCompaniesByRole(project, CompanyProjectRole.CLIENT_COMPANY);
        List<Member> clientManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_MANAGER);
        List<Member> clientMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.CLI_PARTICIPANT);

        // 응답 DTO 생성
        return ProjectCreateResponse.from(project, clientCompanies, clientManagers, clientMembers);
    }

    private void assignCompanyAndMembersToProject(ProjectCreateRequest request, Project project) {
        List<Member> clientManagers = memberService.findByIds(request.getClientMangerIds());
        List<Member> clientMembers = memberService.findByIds(request.getClientMembersIds());

        for (Long companyId : request.getClientCompanyIds()) {
            assignCompanyAndMembers(companyId, clientManagers, project, CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_MANAGER);
            assignCompanyAndMembers(companyId, clientMembers, project, CompanyProjectRole.CLIENT_COMPANY, MemberProjectRole.CLI_PARTICIPANT);
        }
    }

    private void assignCompanyAndMembers(Long companyId, List<Member> members, Project project, CompanyProjectRole companyRole, MemberProjectRole memberRole) {
        Company company = companyService.getCompany(companyId);
        companyProjectService.assignCompanyToProject(company, project, companyRole);
        memberProjectService.assignMembersToProject(company, members, project, memberRole);
    }

    private Project createProjectEntity(ProjectCreateRequest request) {
        // DTO 생성
        ProjectCommand projectCommand = ProjectCommand.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ProjectStatus.CONTRACT) // 고정값으로 지정
                .build();

        // DTO → Entity 변환 후 저장
        Project project = projectCommand.toEntity();
        return projectRepository.save(project);
    }


    private void validateProjectDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
    }

    public Project getValidProject(Long projectId) {
        // TODO 로직 재구현
        return null;
    }
}
