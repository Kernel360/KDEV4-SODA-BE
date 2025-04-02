package com.soda.project;

import lombok.Getter;

@Getter
public class StageCreateRequest {
    private Long projectId;
    private String name;
    private Long prevStageId;
    private Long nextStageId;
}
