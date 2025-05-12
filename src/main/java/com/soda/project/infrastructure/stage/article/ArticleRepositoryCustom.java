package com.soda.project.infrastructure.stage.article;

import com.querydsl.core.Tuple;
import com.soda.project.interfaces.stage.article.dto.ArticleSearchCondition;
import com.soda.project.domain.stage.article.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ArticleRepositoryCustom {

    Page<Tuple> findMyArticlesData(Long authorId, Long projectId, Pageable pageable);

    Optional<Article> findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(Long articleId);

    Page<Article> searchArticles(Long projectId, ArticleSearchCondition articleSearchCondition, Pageable pageable);
}
