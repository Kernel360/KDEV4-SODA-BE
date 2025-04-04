package com.soda.project.stage.task;

import lombok.Getter;

@Getter
public class TaskMoveRequest {
    private Long prevTaskId;
    private Long nextTaskId;
}
