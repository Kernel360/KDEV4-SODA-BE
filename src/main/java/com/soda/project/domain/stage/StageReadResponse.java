package com.soda.project.domain.stage;

import com.soda.project.entity.Stage;
import lombok.Getter;

@Getter
public class StageReadResponse {
    private Long id;
    private String name;
    private Float stageOrder;

    private StageReadResponse() {} // 직접 생성 방지

    public static StageReadResponse fromEntity(Stage stage) {
        StageReadResponse dto = new StageReadResponse();
        dto.id = stage.getId();
        dto.name = stage.getName();
        dto.stageOrder = stage.getStageOrder();
        return dto;
    }
}
