package com.soda.project.application.validator;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
import com.soda.project.domain.ProjectErrorCode;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.member.enums.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProjectValidator {

    private static final String ADMIN_ROLE = "ADMIN";

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
}
