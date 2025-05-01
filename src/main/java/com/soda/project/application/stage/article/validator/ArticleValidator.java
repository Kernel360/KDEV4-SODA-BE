package com.soda.project.application.stage.article.validator;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleValidator {

    public void validateArticle(Article article, Long articleId) {
        if (article == null) {
            throw new GeneralException(ArticleErrorCode.INVALID_ARTICLE);
        }
    }
}
