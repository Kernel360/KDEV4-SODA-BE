package com.soda.notice.enums;

import lombok.Getter;

@Getter
public enum NoticeType {
    STAGE("프로젝트 개발 단계"),
    TASK("프로젝트 단계별 테스크")
    ;

    private final String description;

    NoticeType(String description) {
        this.description = description;
    }

}
