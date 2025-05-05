package com.soda.project.domain.stage.article.vote;

import java.util.List;

public interface VoteAnswerProvider {
    VoteAnswer storeAnswerWithItems(VoteAnswer voteAnswer);

    // 중복 투표 확인용 메서드
    boolean hasUserVoted(Long voteId, Long userId);

    // 결과 집계용 메서드
    int countAnswersByVote(Long voteId);

    List<String> findTextAnswersByVote(Long voteId);
}
