package com.soda.project.infrastructure;

import java.util.Map;

public interface VoteAnswerItemRepositoryCustom {

    Map<Long, Long> countItemGroupByVoteItemId(Long voteId);
}
