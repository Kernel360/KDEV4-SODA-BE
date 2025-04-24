package com.soda.project.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectListWithStatsResponse {
    private Long id;
    private String title;
    private Long weeklyRequestCount;
    private Long weeklyArticleCount;
    private Long weeklyActivity;
    private LocalDateTime recentActivityDate;

    public ProjectListWithStatsResponse(Long id, String title,
                                        Long weeklyRequestCount,
                                        Long weeklyArticleCount,
                                        Long weeklyActivity,
                                        LocalDateTime recentRequestDate,
                                        LocalDateTime recentArticleDate) {
        this.id = id;
        this.title = title;
        this.weeklyRequestCount = weeklyRequestCount;
        this.weeklyArticleCount = weeklyArticleCount;
        this.weeklyActivity = weeklyActivity;

        this.recentActivityDate = recentRequestDate == null ? recentArticleDate
                : recentArticleDate == null ? recentRequestDate
                : recentRequestDate.isAfter(recentArticleDate) ? recentRequestDate : recentArticleDate;
    }
}


