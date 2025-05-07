package com.soda.project.application.validator;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.*;
import com.soda.member.domain.company.Company;
import com.soda.member.domain.company.CompanyService;
import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.member.MemberProjectRole;
import com.soda.project.domain.member.MemberProjectService;
import com.soda.project.interfaces.dto.CompanyAssignment;
import com.soda.project.interfaces.dto.DevCompanyAssignmentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private static final String ADMIN_ROLE = "ADMIN";

    private final CompanyService companyService;
    private final MemberService memberService;
    private final MemberProjectService memberProjectService;

    public void validateProjectAuthority(Member member, Long projectId) {
        if (!isCliInCurrentProject(projectId, member) && !isAdmin(member.getRole())) {
            throw new GeneralException(CommonErrorCode.USER_NOT_IN_PROJECT_CLI);
        }
    }

    private static boolean isCliInCurrentProject(Long projectId, Member member) {
        return member.getMemberProjects().stream()
                .anyMatch(mp ->
                        mp.getProject().getId().equals(projectId) &&
                                (mp.getRole() == MemberProjectRole.CLI_MANAGER || mp.getRole() == MemberProjectRole.CLI_PARTICIPANT)
                );
    }

    private static boolean isAdmin(MemberRole memberRole) {
        return memberRole == MemberRole.ADMIN;
    }

    public void validateProjectDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
    }

    public void validateAdminRole(String userRole) {
        if (!ADMIN_ROLE.equals(userRole)) {
            throw new GeneralException(ProjectErrorCode.UNAUTHORIZED_USER);
        }
    }

    public void validateDevAssignments(DevCompanyAssignmentRequest request, Project project) {
        List<CompanyAssignment> assignments = request.getDevAssignments();

        // 각 assignment 에 대해 유효성 검증 수행
        for (CompanyAssignment assignment : assignments) {
            validateSingleAssignment(assignment, project);
        }
    }

    public void validateSingleAssignment(CompanyAssignment assignment, Project project) {
        if (assignment == null || assignment.getCompanyId() == null) {
            throw new GeneralException(ProjectErrorCode.COMPANY_PROJECT_NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(assignment.getManagerIds())) {
            throw new GeneralException(ProjectErrorCode.MEMBER_LIST_EMPTY);
        }
        Company company = companyService.getCompany(assignment.getCompanyId());
        List<Long> allMemberIds = Stream.concat(
                assignment.getManagerIds().stream(),
                (assignment.getMemberIds() != null ? assignment.getMemberIds().stream() : Stream.empty())
        ).distinct().collect(Collectors.toList());
        if (!allMemberIds.isEmpty()) {
            List<Member> members = memberService.findByIds(allMemberIds);
            validateMembersBelongToCompany(members, company);
        }
    }

    private void validateMembersBelongToCompany(List<Member> members, Company company) {
        if (CollectionUtils.isEmpty(members)) return;
        Long expectedCompanyId = company.getId();
        for (Member member : members) {
            if (member.getCompany() == null || !member.getCompany().getId().equals(expectedCompanyId)) {
                throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_SPECIFIED_COMPANY);
            }
        }
    }

    public void validateProjectAccessPermission(Member member, Project project) {
        // 1. 관리자 확인
        if (member.getRole() == MemberRole.ADMIN) {
            return;
        }
        // 2. 프로젝트 멤버 확인
        boolean isParticipant = memberProjectService.existsByMemberAndProjectAndIsDeletedFalse(member, project);
        if (!isParticipant) {
            throw new GeneralException(ProjectErrorCode.MEMBER_NOT_IN_PROJECT);
        }
    }

    public void validateStatsDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
        if (startDate.isAfter(endDate)) {
            throw new GeneralException(ProjectErrorCode.INVALID_DATE_RANGE);
        }
    }
}
