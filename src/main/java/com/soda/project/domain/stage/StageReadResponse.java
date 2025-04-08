package com.soda.project.domain.stage;

import com.soda.project.domain.task.TaskReadResponse;
import com.soda.project.entity.Stage;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class StageReadResponse {
    private Long id;
    private String name;
    private Float stageOrder;
    private List<TaskReadResponse> tasks;

    private StageReadResponse() {}

    public static StageReadResponse fromEntity(Stage stage) {
        StageReadResponse dto = new StageReadResponse();
        dto.id = stage.getId();
        dto.name = stage.getName();
        dto.stageOrder = stage.getStageOrder();
        dto.tasks = stage.getTaskList().stream()
                .map(TaskReadResponse::fromEntity)
                .collect(Collectors.toList());
        return dto;
    }
}
