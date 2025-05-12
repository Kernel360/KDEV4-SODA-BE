package com.soda.project.domain.member;

import lombok.Getter;

@Getter
public enum MemberProjectRole {

    DEV_MANAGER("개발사담당자"),
    DEV_PARTICIPANT("개발사일반참여자"),
    CLI_MANAGER("고객사담당자"),
    CLI_PARTICIPANT("고객사일반참여자");

    private final String description;

    MemberProjectRole(String description) {
        this.description = description;
    }
}
