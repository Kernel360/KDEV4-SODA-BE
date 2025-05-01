package com.soda.project.interfaces.dto;

import com.soda.project.domain.enums.ProjectStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectListResponse {

    private Long id;
    private String title;
    private ProjectStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Long weeklyRequestCount;
    private Long weeklyArticleCount;
    private Long weeklyActivity;
    private LocalDateTime recentActivityDate;

    public ProjectListResponse(Long id, String title, ProjectStatus status,
                               LocalDateTime startDate, LocalDateTime endDate,
                               Long weeklyRequestCount, Long weeklyArticleCount, Long weeklyActivity,
                               LocalDateTime recentRequestDate, LocalDateTime recentArticleDate) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weeklyRequestCount = weeklyRequestCount;
        this.weeklyArticleCount = weeklyArticleCount;
        this.weeklyActivity = weeklyActivity;
        this.recentActivityDate = resolveLatestDate(recentRequestDate, recentArticleDate);
    }

//    public static ProjectListResponse from(Project project) {
//        return ProjectListResponse.builder()
//                .id(project.getId())
//                .title(project.getTitle())
//                .status(project.getStatus())
//                .startDate(project.getStartDate())
//                .endDate(project.getEndDate())
//                .build();
//    }

    private LocalDateTime resolveLatestDate(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }
}
