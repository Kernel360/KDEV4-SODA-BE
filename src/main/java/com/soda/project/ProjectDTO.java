package com.soda.project;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectDTO {
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder
    public ProjectDTO(String title,String description, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Entity → DTO 변환
    public static ProjectDTO fromEntity(Project project) {
        return ProjectDTO.builder()
                .title(project.getTitle())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }

    // DTO → Entity 변환
    public Project toEntity() {
        return Project.builder()
                .title(this.title)
                .description(this.description)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}
