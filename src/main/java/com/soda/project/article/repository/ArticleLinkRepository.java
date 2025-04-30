package com.soda.project.article.repository;

import com.soda.project.domain.stage.article.ArticleLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleLinkRepository extends JpaRepository<ArticleLink, Long> {
    List<ArticleLink> findByArticleId(Long id);

    Optional<ArticleLink> findByArticleIdAndUrlAddressAndIsDeletedTrue(Long articleId, String urladdress);
}
