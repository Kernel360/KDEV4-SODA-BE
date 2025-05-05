package com.soda.project.interfaces.stage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StageUpdateRequest {
    @NotBlank(message = "단계 이름은 필수입니다.")
    private String name;
}
