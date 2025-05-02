package com.soda.project.domain.stage.article.comment;

import com.soda.project.domain.stage.article.Article;

import java.util.List;

public interface CommentProvider {
    Comment store(Comment comment);

    List<Comment> findAllByArticle(Article article);
}
