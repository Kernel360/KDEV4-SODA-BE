package com.soda.project.domain;

import com.soda.project.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectStatusUpdateRequest {

    @NotNull(message = "변경할 프로젝트 상태는 필수입니다.")
    private ProjectStatus status;

}
