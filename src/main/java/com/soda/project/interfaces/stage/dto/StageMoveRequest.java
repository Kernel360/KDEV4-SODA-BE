package com.soda.project.interfaces.stage.dto;

import lombok.Getter;

@Getter
public class StageMoveRequest {
    private Long prevStageId;
    private Long nextStageId;
}
