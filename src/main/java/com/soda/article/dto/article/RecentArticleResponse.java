package com.soda.article.dto.article;

import com.soda.article.entity.Article;
import com.soda.project.entity.Project;
import com.soda.project.entity.Stage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentArticleResponse {

    private Long projectId;
    private String projectTitle;
    private Long articleId;
    private String articleTitle;
    private LocalDateTime createdAt;

    public static RecentArticleResponse from(Article article) {
        Stage stage = article.getStage();
        Project project = stage.getProject() ;

        return RecentArticleResponse.builder()
                .projectId(project.getId())
                .projectTitle(project.getTitle())
                .articleId(article.getId())
                .articleTitle(article.getTitle())
                .createdAt(article.getCreatedAt())
                .build();
    }
}
