package com.soda.article.repository;

import com.querydsl.core.Tuple;
import com.soda.article.dto.article.ArticleSearchCondition;
import com.soda.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleRepositoryCustom {

    Page<Tuple> findMyArticlesData(Long authorId, Long projectId, Pageable pageable);

    Optional<Article> findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(Long articleId);

    List<Article> searchArticles(Long projectId, ArticleSearchCondition articleSearchCondition);
}
