package com.soda.project.domain.stage.request;

public enum RequestStatus {
    APPROVED("승인됨"),
    APPROVING("승인중"),
    PENDING("대기중"),
    REJECTED("거절됨")
    ;

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }
}
