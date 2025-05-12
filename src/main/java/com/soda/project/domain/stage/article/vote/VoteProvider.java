package com.soda.project.domain.stage.article.vote;

import java.util.Optional;

public interface VoteProvider {
    Vote store(Vote vote);

    boolean existsByArticleIdAndIsDeletedFalse(Long articleId);

    Optional<Vote> findById(Long voteId);
}
