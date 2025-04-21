package com.soda.request.enums;

public enum ResponseStatus {
    APPROVED("승인됨"),
    REJECTED("거절됨")
    ;

    private final String description;

    ResponseStatus(String description) {
        this.description = description;
    }
}
