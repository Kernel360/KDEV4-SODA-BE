package com.soda.entity.enums;

import lombok.Getter;

@Getter
public enum FileType {
    ARTICLE("게시글"),
    TASK("단계 내부 항목"),
    REQUEST("승인 요청")
    ;

    private final String description;

    FileType(String description) {
        this.description = description;
    }
}
