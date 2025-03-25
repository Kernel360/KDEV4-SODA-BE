package com.soda.article.repository;

import com.soda.article.entity.ArticleLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleLinkRepository extends JpaRepository<ArticleLink, Long> {
    List<ArticleLink> findByArticleId(Long id);
}
