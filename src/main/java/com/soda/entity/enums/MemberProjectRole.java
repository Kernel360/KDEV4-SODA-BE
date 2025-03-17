package com.soda.entity.enums;

import lombok.Getter;

@Getter
public enum MemberProjectRole {

    MANAGER("담당자"),
    PARTICIPANT("일반참여자");

    private final String description;

    MemberProjectRole(String description) {
        this.description = description;
    }
}
