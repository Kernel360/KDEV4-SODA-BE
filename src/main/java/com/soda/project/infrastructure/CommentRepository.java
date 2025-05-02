package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 댓글 및 대댓글 모두 조회
    List<Comment> findByArticleAndIsDeletedFalse(Article article);

    Optional<Comment> findByIdAndIsDeletedFalse(Long commentId);

    List<Comment> findAllByArticle(Article article);
}
