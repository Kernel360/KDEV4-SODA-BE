package com.soda.entity.enums;

import lombok.Getter;

@Getter
public enum LinkType {
    ARTICLE("게시글"),
    TASK("단계 내부 항목"),
    REQUEST("승인 요청")
    ;

    private final String description;

    LinkType(String description) {
        this.description = description;
    }
}
