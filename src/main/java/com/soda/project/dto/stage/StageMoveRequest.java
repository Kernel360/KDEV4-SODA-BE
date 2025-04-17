package com.soda.project.dto.stage;

import lombok.Getter;

@Getter
public class StageMoveRequest {
    private Long prevStageId;
    private Long nextStageId;
}
