package com.soda.member.enums;

import lombok.Getter;

@Getter
public enum MemberRole {
    ADMIN("관리자"),
    USER("일반사용자"),
    ;

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

}
