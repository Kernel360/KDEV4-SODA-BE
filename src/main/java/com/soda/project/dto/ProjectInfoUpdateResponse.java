package com.soda.project.dto;

import com.soda.project.entity.Project;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectInfoUpdateResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public static ProjectInfoUpdateResponse from(Project project) {
        return ProjectInfoUpdateResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }
}
