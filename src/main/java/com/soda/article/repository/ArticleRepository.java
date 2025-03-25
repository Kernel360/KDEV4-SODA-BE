package com.soda.article.repository;

import com.soda.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.articleFileList " +
            "LEFT JOIN FETCH a.articleLinkList " +
            "WHERE a.id = :id")
    Optional<Article> findByIdWithFilesAndLinks(@Param("id") Long id);

}
