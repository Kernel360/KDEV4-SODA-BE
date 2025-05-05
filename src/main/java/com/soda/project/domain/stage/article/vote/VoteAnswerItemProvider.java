package com.soda.project.domain.stage.article.vote;

import java.util.Map;

public interface VoteAnswerItemProvider {
    Map<Long, Long> countItemsByVote(Long voteId);
}
