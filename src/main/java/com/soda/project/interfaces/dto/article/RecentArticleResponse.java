package com.soda.project.interfaces.dto.article;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;
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
