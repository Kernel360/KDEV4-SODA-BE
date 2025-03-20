package com.soda.article.enums;

import lombok.Getter;

@Getter
public enum PriorityType {

    LOW("낮음"),
    MEDIUM("중간"),
    HIGH("높음");

    private final String description;

    PriorityType (String description) {
        this.description = description;
    }
}
