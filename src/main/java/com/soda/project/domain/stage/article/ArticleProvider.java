package com.soda.project.domain.stage.article;

import com.querydsl.core.Tuple;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;
import com.soda.project.interfaces.dto.stage.article.ArticleSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ArticleProvider {
    Article store(Article article);

    Optional<Article> findById(Long id);

    Page<Article> searchArticles(Long projectId, ArticleSearchCondition searchCondition, Pageable pageable);

    List<Article> findByStageAndStage_Project(Stage stage, Project project);

    List<Article> findByStage_Project(Project project);

    Optional<Article> findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(Long articleId);

    Page<Tuple> findMyArticlesData(Long userId, Long projectId, Pageable pageable);
}
