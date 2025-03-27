package com.soda.article.repository;

import com.soda.article.entity.Article;
import com.soda.article.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 및 대댓글 모두 조회
    List<Comment> findByArticleAndIsDeletedFalse(Article article);

    Optional<Comment> findByIdAndIsDeletedFalse(Long commentId);
}
