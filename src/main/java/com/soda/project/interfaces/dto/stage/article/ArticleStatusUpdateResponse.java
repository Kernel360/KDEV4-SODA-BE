package com.soda.project.interfaces.dto.stage.article;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleStatusUpdateResponse {

    private Long articleId;
    private ArticleStatus status;

    public static ArticleStatusUpdateResponse from (Article article) {
        return ArticleStatusUpdateResponse.builder()
                .articleId(article.getId())
                .status(article.getStatus())
                .build();
    }
}
