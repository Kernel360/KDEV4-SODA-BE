package com.soda.project.article.repository;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.Project;
import com.soda.project.domain.stage.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom{
    // isDeleted가 false인 게시글만 조회하고, 특정 Project에 속한 게시글만 조회
    List<Article> findByIsDeletedFalseAndStage_Project(Project project);

    Optional<Article> findByIdAndIsDeletedFalse(Long articleId);

    List<Article> findByIsDeletedFalseAndStageAndStage_Project(Stage stage, Project project);

    List<Article> findByStage_Project(Project project);

    List<Article> findByStageAndStage_Project(Stage stage, Project project);

    List<Article> findTop3ByStage_IdInAndIsDeletedFalseOrderByCreatedAtDesc(List<Long> stageIds);
}
