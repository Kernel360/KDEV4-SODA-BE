package com.soda.project.service;

import com.soda.global.log.dataLog.annotation.LoggableEntityAction;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Company;
import com.soda.member.entity.Member;
import com.soda.member.service.CompanyService;
import com.soda.member.service.MemberService;
import com.soda.project.dto.*;
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
    @LoggableEntityAction(action = "CREATE", entityClass = Project.class)
    @Transactional
    public ProjectCreateResponse createProject(String userRole, ProjectCreateRequest request) {
        // 사용자 유효성 검사 관리자만 프로젝트 생성 가능
        validateAdminRole(userRole);

        // 날짜 순서 유효성 검사
        validateProjectDates(request.getStartDate(), request.getEndDate());

        // 프로젝트 기본 정보 생성
        Project project = createProjectEntity(request);

        // 고객사 지정
        assignClientCompanies(request, project);

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

    private Project createProjectEntity(ProjectCreateRequest request) {
        // DTO 생성
        ProjectDTO projectDTO = ProjectDTO.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(ProjectStatus.CONTRACT) // 고정값으로 지정
                .build();

        // DTO → Entity 변환 후 저장
        Project project = projectDTO.toEntity();
        return projectRepository.save(project);
    }

    private void validateProjectDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
    }
    
    /**
     * 기존 프로젝트에 개발사 및 해당 개발사의 담당자/참여자를 지정하는 메서드
     *
     * @param projectId 프로젝트 ID (기존에 존재하는 프로젝트)
     * @param userRole 현재 요청자의 역할 (ADMIN만 허용)
     * @param request 개발사 및 구성원 지정 요청 정보를 담은 DTO
     * @return 개발사 이름, 담당자 목록, 일반 참여자 목록이 포함된 응답 DTO
     * @throws GeneralException 프로젝트가 존재하지 않거나, 요청자가 ADMIN 아닐 경우 예외 발생
     */
    // TODO 우선 log 안달고 진행
    @Transactional
    public DevCompanyAssignmentResponse assignDevCompany(Long projectId, String userRole, DevCompanyAssignmentRequest request) {
        // project 유효성 검사
        Project project = getValidProject(projectId);

        // ADMIN 확인
        validateAdminRole(userRole);

        // 고객사 지정
        assignDevCompanies(request, project);

        // response 생성
        return createDevCompanyAssignmentResponse(project);
    }

    private DevCompanyAssignmentResponse createDevCompanyAssignmentResponse(Project project) {
        List<Company> devCompanies = companyProjectService.getClientCompaniesByRole(project, CompanyProjectRole.DEV_COMPANY);
        List<Member> devManagers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_MANAGER);
        List<Member> devMembers = memberProjectService.getMembersByRole(project, MemberProjectRole.DEV_PARTICIPANT);

        return DevCompanyAssignmentResponse.from(devCompanies, devManagers, devMembers);
    }


    private void assignClientCompanies(ProjectCreateRequest request, Project project) {
        assignCompaniesAndMembers(
                request.getClientCompanyIds(),
                request.getClientMangerIds(),
                request.getClientMemberIds(),
                project,
                CompanyProjectRole.CLIENT_COMPANY,
                MemberProjectRole.CLI_MANAGER,
                MemberProjectRole.CLI_PARTICIPANT
        );
    }

    void assignDevCompanies(DevCompanyAssignmentRequest request, Project project) {
        assignCompaniesAndMembers(
                request.getDevCompanyIds(),
                request.getDevMangerIds(),
                request.getDevMemberIds(),
                project,
                CompanyProjectRole.DEV_COMPANY,
                MemberProjectRole.DEV_MANAGER,
                MemberProjectRole.DEV_PARTICIPANT
        );
    }

    private void assignCompaniesAndMembers( List<Long> companyIds,
                                            List<Long> managerIds,
                                            List<Long> memberIds,
                                            Project project,
                                            CompanyProjectRole companyRole,
                                            MemberProjectRole managerRole,
                                            MemberProjectRole memberRole) {
        List<Member> managers = memberService.findByIds(managerIds);
        List<Member> members = memberService.findByIds(memberIds);

        for (Long companyId : companyIds) {
            Company company = companyService.getCompany(companyId);
            companyProjectService.assignCompanyToProject(company, project, companyRole);
            memberProjectService.assignMembersToProject(company, managers, project, managerRole);
            memberProjectService.assignMembersToProject(company, members, project, memberRole);
        }
    }

    private void validateAdminRole(String userRole) {
        if (!ADMIN_ROLE.equals(userRole)) {
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }
    }

    public Project getValidProject(Long projectId) {
        return projectRepository.findByIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new GeneralException(ProjectErrorCode.PROJECT_NOT_FOUND));
    }
}
