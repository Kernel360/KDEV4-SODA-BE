package com.soda.project.interfaces.dto.stage.article.vote;

import com.soda.project.domain.stage.article.vote.VoteItem;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteItemAddResponse {

    private Long voteId;
    private Long itemId;
    private String itemText;

    public static VoteItemAddResponse from(VoteItem voteItem) {
        return VoteItemAddResponse.builder()
                .voteId(voteItem.getVote().getId())
                .itemId(voteItem.getId())
                .itemText(voteItem.getText())
                .build();
    }
}
