package com.soda.project.interfaces.stats;

import com.soda.project.domain.Project;
import com.soda.project.domain.ProjectStatus;
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
