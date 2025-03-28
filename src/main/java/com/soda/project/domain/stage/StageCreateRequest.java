package com.soda.project.domain.stage;

import lombok.Getter;

@Getter
public class StageCreateRequest {
    private Long projectId;
    private String name;
    private Float newOrder;
}
