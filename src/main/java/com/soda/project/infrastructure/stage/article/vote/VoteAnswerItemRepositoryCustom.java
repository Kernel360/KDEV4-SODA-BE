package com.soda.project.infrastructure.stage.article.vote;

import java.util.Map;

public interface VoteAnswerItemRepositoryCustom {

    Map<Long, Long> countItemGroupByVoteItemId(Long voteId);
}
