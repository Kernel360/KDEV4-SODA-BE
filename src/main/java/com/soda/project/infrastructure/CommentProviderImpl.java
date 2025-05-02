package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.comment.Comment;
import com.soda.project.domain.stage.article.comment.CommentProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentProviderImpl implements CommentProvider {
    private final CommentRepository commentRepository;

    @Override
    public Comment store(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public List<Comment> findAllByArticle(Article article) {
        return commentRepository.findAllByArticle(article);
    }
}
