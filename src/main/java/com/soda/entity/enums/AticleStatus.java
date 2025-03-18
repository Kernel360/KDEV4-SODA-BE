package com.soda.entity.enums;

import lombok.Getter;

@Getter
public enum ArticleStatus {
    COMMENTED("답변완료"),
    PENDING("답변대기")
    ;

    private final String description;

    ArticleStatus(String description) {
        this.description = description;
    }

}
