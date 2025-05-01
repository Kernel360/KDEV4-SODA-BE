package com.soda.project.domain.stage.article;

import com.soda.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class VoteAnswerItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_response_id", nullable = false)
    private VoteAnswer voteResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_item_id", nullable = false)
    private VoteItem voteItem;

    @Builder
    public VoteAnswerItem(VoteAnswer voteResponse, VoteItem voteItem) {
        this.voteResponse = voteResponse;
        this.voteItem = voteItem;
    }

    public void delete() {
        this.markAsDeleted();
    }
}
