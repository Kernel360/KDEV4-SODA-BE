package com.soda.article.entity;

import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class VoteItem extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @OneToMany(mappedBy = "voteItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteAnswerItem> responses = new ArrayList<>();

    @Builder
    public VoteItem(String text, Vote vote) {
        this.text = text;
        this.vote = vote;
    }

    protected void setVote(Vote vote) {
        this.vote = vote;
    }

    public void delete() {
        this.markAsDeleted();
        if (this.responses != null) {
            this.responses.forEach(VoteAnswerItem::delete);
        }
    }
}
