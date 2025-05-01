package com.soda.project.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectInfoUpdateRequest {

    @NotBlank(message = "프로젝트 제목은 필수입니다.")
    @Size(max = 255, message = "프로젝트 제목은 255자를 넘을 수 없습니다.")
    private String title;

    @Size(max = 5000, message = "프로젝트 설명은 5000자를 넘을 수 없습니다.")
    private String description;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

}
