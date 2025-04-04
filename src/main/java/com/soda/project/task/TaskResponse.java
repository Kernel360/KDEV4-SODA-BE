package com.soda.project.task;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TaskResponse {
    private Long taskId;
    private Long stageId;
    private String title;
    private String content;
    private Float taskOrder;

    @Builder
    public TaskResponse(Long taskId, Long stageId, String title, String content, Float taskOrder) {
        this.taskId = taskId;
        this.stageId = stageId;
        this.title = title;
        this.content = content;
        this.taskOrder = taskOrder;
    }

    public static TaskResponse fromEntity(Task task) {
        if (task == null) return null;
        return TaskResponse.builder()
                .taskId(task.getId())
                .stageId(task.getStage() != null ? task.getStage().getId() : null)
                .title(task.getTitle())
                .content(task.getContent())
                .taskOrder(task.getTaskOrder())
                .build();
    }
}