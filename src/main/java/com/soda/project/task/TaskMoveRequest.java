package com.soda.project.task;

import lombok.Getter;

@Getter
public class TaskMoveRequest {
    private Long prevTaskId;
    private Long nextTaskId;
}
