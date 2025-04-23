package com.soda.article.repository;

import java.util.Map;

public interface VoteAnswerItemRepositoryCustom {

    Map<Long, Long> countItemGroupByVoteItemId(Long voteId);
}
