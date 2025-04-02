package com.soda.article.repository;

import com.soda.article.entity.Article;
import com.soda.project.Project;
import com.soda.project.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // isDeleted가 false인 게시글만 조회하고, 특정 Project에 속한 게시글만 조회
    List<Article> findByIsDeletedFalseAndStage_Project(Project project);

    Optional<Article> findByIdAndIsDeletedFalse(Long articleId);

    List<Article> findByIsDeletedFalseAndStageAndStage_Project(Stage stage, Project project);

}
