package com.soda.project.domain.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    CONTRACT("계약"),
    IN_PROGRESS("진행중"),
    DELIVERED("납품완료"),
    MAINTENANCE("하자보수"),
    ON_HOLD("일시중단");

    private final String description;

    ProjectStatus(String description) {
        this.description = description;
    }
}
