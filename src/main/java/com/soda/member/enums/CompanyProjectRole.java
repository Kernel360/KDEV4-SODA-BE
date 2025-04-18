package com.soda.member.enums;

import lombok.Getter;

@Getter
public enum CompanyProjectRole {
    DEV_COMPANY("개발사", MemberProjectRole.DEV_MANAGER, MemberProjectRole.DEV_PARTICIPANT),
    CLIENT_COMPANY("고객사", MemberProjectRole.CLI_MANAGER, MemberProjectRole.CLI_PARTICIPANT);

    private final String description;
    private final MemberProjectRole managerRole;
    private final MemberProjectRole memberRole;

    CompanyProjectRole(String description, MemberProjectRole managerRole, MemberProjectRole memberRole) {
        this.description = description;
        this.managerRole = managerRole;
        this.memberRole = memberRole;
    }

}
