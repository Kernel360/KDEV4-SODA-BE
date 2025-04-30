package com.soda.project.article.dto.article;

import com.soda.project.domain.stage.article.VoteItem;
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
