package com.soda.article.dto.article;

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

    public static MyArticleListResponse from() {
        return null;
    }
}
