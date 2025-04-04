package com.soda.project.stage.task;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TaskReadResponse {
    private Long taskId;
    private String title;
    private String content;
    private Float taskOrder;
    // 필요하다면 다른 필드 추가 (e.g., Request 개수 등)

    @Builder
    public TaskReadResponse(Long taskId, String title, String content, Float taskOrder) {
        this.taskId = taskId;
        this.title = title;
        this.content = content;
        this.taskOrder = taskOrder;
    }

    public static TaskReadResponse fromEntity(Task task) {
        if (task == null) return null;
        return TaskReadResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .content(task.getContent())
                .taskOrder(task.getTaskOrder())
                .build();
    }
}