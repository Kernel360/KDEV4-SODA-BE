package com.soda.article.entity;

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
public class VoteResponseItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_response_id", nullable = false)
    private VoteResponse voteResponse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_item_id", nullable = false)
    private VoteItem voteItem;

    @Builder
    public VoteResponseItem(VoteResponse voteResponse, VoteItem voteItem) {
        this.voteResponse = voteResponse;
        this.voteItem = voteItem;
    }

    public void delete() {
        this.markAsDeleted();
    }
}
