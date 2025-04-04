package com.soda.project.stage;

import lombok.Getter;

@Getter
public class StageMoveRequest {
    private Long prevStageId;
    private Long nextStageId;
}
