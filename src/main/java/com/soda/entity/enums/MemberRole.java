package com.soda.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum MemberRole {
    ADMIN("관리자"),
    DEV_COMPANY("개발자"),
    CLIENT_COMPANY("고객")
    ;

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

}
