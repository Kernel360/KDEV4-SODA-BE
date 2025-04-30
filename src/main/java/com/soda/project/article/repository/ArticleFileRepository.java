package com.soda.project.article.repository;

import com.soda.project.domain.stage.article.ArticleFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleFileRepository extends JpaRepository<ArticleFile, Long> {
    List<ArticleFile> findByArticleId(Long articleId);

    Optional<ArticleFile> findByArticleIdAndNameAndIsDeletedTrue(Long articleId, String name);
}
