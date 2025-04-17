package com.soda.project.dto.stage;

import com.soda.project.entity.Stage;
import lombok.Getter;

@Getter
public class StageReadResponse {
    private Long id;
    private String name;
    private Float stageOrder;

    private StageReadResponse() {
    }

    /**
     * Stage 엔티티로부터 StageReadResponse DTO를 생성합니다.
     *
     * @param stage 변환할 Stage 엔티티
     * @return 변환된 StageReadResponse DTO
     */
    public static StageReadResponse fromEntity(Stage stage) {
        StageReadResponse dto = new StageReadResponse();
        dto.id = stage.getId();
        dto.name = stage.getName();
        dto.stageOrder = stage.getStageOrder();
        return dto;
    }
}
