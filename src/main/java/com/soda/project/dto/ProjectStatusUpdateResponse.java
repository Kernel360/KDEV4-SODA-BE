package com.soda.project.dto;

import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
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
