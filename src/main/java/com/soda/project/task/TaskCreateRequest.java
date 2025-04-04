package com.soda.project.task;

import lombok.Getter;

@Getter
public class TaskCreateRequest {
    private Long stageId;
    private String title;
    private String content;
    private Long prevTaskId;
    private Long nextTaskId;
}
