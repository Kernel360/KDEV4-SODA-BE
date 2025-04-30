package com.soda.project.interfaces.dto;

import com.soda.project.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectStatusUpdateRequest {

    @NotNull(message = "반경할 프로젝트 상태는 필수입니다.")
    private ProjectStatus status;

}
