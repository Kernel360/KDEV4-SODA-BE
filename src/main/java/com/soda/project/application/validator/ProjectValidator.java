package com.soda.project.application.validator;

import com.soda.global.response.CommonErrorCode;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import com.soda.project.domain.member.enums.MemberProjectRole;
import com.soda.member.domain.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectValidator {
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
}
