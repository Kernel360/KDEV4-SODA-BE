package com.soda.project.interfaces.stage.dto;

import com.soda.project.domain.stage.StageConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class StageCreateRequest {
    @NotNull(message = "프로젝트 ID는 필수입니다")
    private Long projectId;

    @NotBlank(message = "단계 이름은 필수입니다")
    @Size(max = StageConstants.MAX_STAGE_NAME_LENGTH, message = "단계 이름은 " + StageConstants.MAX_STAGE_NAME_LENGTH
            + "자를 초과할 수 없습니다")
    private String name;

    private Long prevStageId;
    private Long nextStageId;
}
