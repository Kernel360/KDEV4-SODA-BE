package com.soda.project;

import lombok.Getter;

@Getter
public class StageMoveRequest {
    private Long prevStageId;
    private Long nextStageId;
}
