package com.soda.project.domain.dto;

import com.soda.project.domain.Project;
import com.soda.project.domain.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectStatusUpdateResponse {

    private Long projectId;
    private ProjectStatus status;

    public static ProjectStatusUpdateResponse from (Project project) {
        return ProjectStatusUpdateResponse.builder()
                .projectId(project.getId())
                .status(project.getStatus())
                .build();
    }
}
