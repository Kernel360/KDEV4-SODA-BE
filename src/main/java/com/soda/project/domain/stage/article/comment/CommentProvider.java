package com.soda.project.domain.stage.article.comment;

import com.soda.project.domain.stage.article.Article;

import java.util.List;
import java.util.Optional;

public interface CommentProvider {
    Comment store(Comment comment);

    List<Comment> findAllByArticle(Article article);

    Optional<Comment> findById(Long commentId);
}
