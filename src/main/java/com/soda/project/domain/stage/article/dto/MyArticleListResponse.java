package com.soda.project.domain.stage.article.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyArticleListResponse {

    private Long projectId;
    private Long articleId;
    private String title;
    private String projectName;
    private Long stageId;
    private String stageName;
    private LocalDateTime createdAt;

    public static MyArticleListResponse from(Long articleId, String title, Long projectId, String projName, Long stageId, String stageName, LocalDateTime createdAt) {
        return MyArticleListResponse.builder()
                .articleId(articleId)
                .title(title)
                .projectId(projectId)
                .projectName(projName)
                .stageId(stageId)
                .stageName(stageName)
                .createdAt(createdAt)
                .build();
    }
}
