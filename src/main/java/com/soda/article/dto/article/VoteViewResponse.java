package com.soda.article.dto.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.entity.Vote;
import com.soda.article.entity.VoteItem;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class VoteViewResponse {

    private Long voteId;
    private String title;
    private boolean isMultipleSelection;
    private LocalDateTime deadLine;
    private boolean closed;
    private List<VoteItemView> items;

    public static VoteViewResponse from(Vote vote) {
        return VoteViewResponse.builder()
                .voteId(vote.getId())
                .title(vote.getTitle())
                .isMultipleSelection(vote.isAllowMultipleSelection())
                .deadLine(vote.getDeadLine())
                .closed(vote.isClosed())
                .items(vote.getVoteItems().stream()
                        .filter(voteItem -> !voteItem.getIsDeleted())
                        .map(VoteItemView::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VoteItemView {
        private Long itemId;
        private String content;

        public static VoteItemView from(VoteItem voteItem) {
            VoteItemView itemView = new VoteItemView();
            itemView.itemId = voteItem.getId();
            itemView.content = voteItem.getText();
            return itemView;
        }
    }
}
