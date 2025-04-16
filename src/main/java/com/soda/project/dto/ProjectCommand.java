package com.soda.project.dto;

import com.soda.project.entity.Project;
import com.soda.project.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectCommand {
    private final String title;
    private final String description;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final ProjectStatus status;

    @Builder
    public ProjectCommand(String title, String description, LocalDateTime startDate, LocalDateTime endDate, ProjectStatus status) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    // Entity → DTO 변환
    public static ProjectCommand fromEntity(Project project) {
        return ProjectCommand.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .build();
    }

    // DTO → Entity 변환
    public Project toEntity() {
        return Project.builder()
                .title(this.title)
                .description(this.description)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .status(this.status)
                .build();
    }
}
