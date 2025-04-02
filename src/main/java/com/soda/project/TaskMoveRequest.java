package com.soda.project;

import lombok.Getter;

@Getter
public class TaskMoveRequest {
    private Long prevTaskId;
    private Long nextTaskId;
}
