package com.soda.article.repository;

import com.soda.article.entity.ArticleFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleFileRepository extends JpaRepository<ArticleFile, Long> {
    List<ArticleFile> findByArticleId(Long articleId);
}
