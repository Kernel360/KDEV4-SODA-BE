package com.soda.project.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectCreateRequest {

    // 프로젝트 기본 정보
    @NotBlank(message = "프로젝트 제목은 필수입니다.")
    @Size(max = 255, message = "프로젝트 제목은 255자를 넘을 수 없습니다.")
    private String title;

    @Size(max = 5000, message = "프로젝트 설명은 5000자를 넘을 수 없습니다.")
    private String description;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

    @NotNull
    private List<String> stageNames;

    // 고객사 지정 (프로젝트 생성 시 계약 단계이므로 고객사만 지정)
    @NotNull
    @Valid
    private List<CompanyAssignment> clientAssignments;

}
