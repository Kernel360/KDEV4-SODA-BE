package com.soda.project.dto;

import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectListResponse {

    private Long id;
    private String title;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static ProjectListResponse from(Project project) {
        return ProjectListResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .status(project.getStatus())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }
}
