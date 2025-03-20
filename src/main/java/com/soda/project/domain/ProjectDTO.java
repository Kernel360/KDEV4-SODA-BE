package com.soda.project.domain;

import com.soda.project.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class ProjectDTO {
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder
    public ProjectDTO(String title, LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Entity → DTO 변환
    public static ProjectDTO fromEntity(Project project) {
        return ProjectDTO.builder()
                .title(project.getTitle())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .build();
    }

    // DTO → Entity 변환
    public Project toEntity() {
        return Project.builder()
                .title(this.title)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .build();
    }
}
