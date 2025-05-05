package com.soda.project.domain.stage.article.vote;

public interface VoteProvider {
    Vote store(Vote vote);

    boolean existsByArticleIdAndIsDeletedFalse(Long articleId);
}
