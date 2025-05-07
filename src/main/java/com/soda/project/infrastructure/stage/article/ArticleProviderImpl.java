package com.soda.project.infrastructure.stage.article;

import com.querydsl.core.Tuple;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleProvider;
import com.soda.project.interfaces.stage.article.dto.ArticleSearchCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ArticleProviderImpl implements ArticleProvider {
    private final ArticleRepository articleRepository;

    @Override
    public Article store(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    public Page<Article> searchArticles(Long projectId, ArticleSearchCondition searchCondition, Pageable pageable) {
        return articleRepository.searchArticles(projectId, searchCondition, pageable);
    }

    @Override
    public List<Article> findByStageAndStage_Project(Stage stage, Project project) {
        return articleRepository.findByStageAndStage_Project(stage, project);
    }

    @Override
    public List<Article> findByStage_Project(Project project) {
        return articleRepository.findByStage_Project(project);
    }

    @Override
    public Optional<Article> findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(Long articleId) {
        return articleRepository.findByIdAndIsDeletedFalseWithMemberAndCompanyUsingQuerydsl(articleId);
    }

    @Override
    public Page<Tuple> findMyArticlesData(Long userId, Long projectId, Pageable pageable) {
        return articleRepository.findMyArticlesData(userId, projectId, pageable);
    }
}
