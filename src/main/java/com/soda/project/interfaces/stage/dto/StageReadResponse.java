package com.soda.project.interfaces.stage.dto;

import com.soda.project.domain.stage.Stage;
import lombok.Getter;

@Getter
public class StageReadResponse {
    private Long id;
    private String name;
    private Float stageOrder;
    private int requestCount;

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
        dto.requestCount = stage.getRequestList().size();
        return dto;
    }
}
