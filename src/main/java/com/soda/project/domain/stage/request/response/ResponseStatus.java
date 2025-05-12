package com.soda.project.domain.stage.request.response;

public enum ResponseStatus {
    APPROVED("승인됨"),
    REJECTED("거절됨")
    ;

    private final String description;

    ResponseStatus(String description) {
        this.description = description;
    }
}
