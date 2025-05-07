package com.soda.project.domain.stage.article.vote;

import com.soda.project.interfaces.stage.article.vote.VoteResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VoteResultCalculator {

    private final VoteAnswerProvider voteAnswerProvider;
    private final VoteAnswerItemProvider voteAnswerItemProvider;

    public VoteResultResponse calculateResults(Vote vote) {
        Long voteId = vote.getId();

        // 1. 총 참여자 수 조회
        int totalParticipants = voteAnswerProvider.countAnswersByVote(voteId);

        Map<Long, Long> itemCounts = Collections.emptyMap();
        List<String> textAnswers = Collections.emptyList();

        // 2. 투표 유형에 따라 결과 집계 (각 서비스 사용)
        if (!vote.isAllowTextAnswer()) { // 항목 투표
            // 항목별 득표 수 집계
            itemCounts = voteAnswerItemProvider.countItemsByVote(vote.getId());
        } else { // 텍스트 투표
            // 텍스트 답변 목록 조회
            textAnswers = voteAnswerProvider.findTextAnswersByVote(vote.getId());
        }

        return VoteResultResponse.from(vote, totalParticipants, itemCounts, textAnswers);
    }
}
