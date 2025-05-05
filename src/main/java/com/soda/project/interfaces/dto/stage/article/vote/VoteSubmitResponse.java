package com.soda.project.interfaces.dto.stage.article.vote;

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

    public static VoteSubmitResponse from(VoteSubmitResponse response) {
        return VoteSubmitResponse.builder()
                .voteId(response.voteId)
                .voterId(response.voterId)
                .voteAnswerId(response.voteAnswerId)
                .selectedItemIds(response.selectedItemIds)
                .build();
    }
}
