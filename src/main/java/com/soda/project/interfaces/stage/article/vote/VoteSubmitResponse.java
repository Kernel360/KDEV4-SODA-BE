package com.soda.project.interfaces.stage.article.vote;

import com.soda.project.domain.stage.article.vote.VoteAnswer;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VoteSubmitResponse {

    private Long voteId;
    private Long voterId;
    private Long voteAnswerId;
    private List<Long> selectedItemIds;

    public static VoteSubmitResponse from(VoteAnswer savedAnswer, List<Long> submittedItemIds) {
        return VoteSubmitResponse.builder()
                .voteId(savedAnswer.getVote().getId())
                .voterId(savedAnswer.getMember().getId())
                .voteAnswerId(savedAnswer.getId())
                .selectedItemIds(submittedItemIds)
                .build();
    }
}
