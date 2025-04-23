package com.soda.article.dto.article;

import com.soda.article.entity.VoteItem;
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
