package com.soda.member.enums;

import lombok.Getter;

@Getter
public enum CompanyProjectRole {
    DEV_COMPANY("개발사"),
    CLIENT_COMPANY("고객사");

    private final String description;

    CompanyProjectRole(String description) {
        this.description = description;
    }

}
