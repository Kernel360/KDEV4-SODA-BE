package com.soda.project.domain.stage;

import lombok.Getter;

@Getter
public class StageMoveRequest {
    private Long prevStageId;
    private Long nextStageId;
}
