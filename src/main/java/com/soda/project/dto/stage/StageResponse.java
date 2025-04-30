package com.soda.project.dto.stage;

import com.soda.project.domain.stage.Stage;
import lombok.Getter;

@Getter
public class StageResponse {
    private Long id;
    private Long projectId;
    private String name;
    private Float stageOrder;

    private StageResponse() {}

    public static StageResponse fromEntity(Stage stage) {
        StageResponse dto = new StageResponse();
        dto.id = stage.getId();
        dto.name = stage.getName();
        dto.stageOrder = stage.getStageOrder();
        dto.projectId = stage.getProject().getId();
        return dto;
    }
}
