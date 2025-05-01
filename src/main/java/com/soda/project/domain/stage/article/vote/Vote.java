package com.soda.project.domain.stage.article.vote;

import com.soda.common.BaseEntity;
import com.soda.project.domain.stage.article.Article;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Vote extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private boolean allowMultipleSelection;

    @Column(nullable = false)
    private boolean allowTextAnswer;

    private LocalDateTime deadLine;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false, unique = true)
    private Article article;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteItem> voteItems = new ArrayList<>();

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteAnswer> voteResponses = new ArrayList<>();

    @Builder
    public Vote(String title, boolean allowMultipleSelection, boolean allowTextAnswer,
                LocalDateTime deadLine, Article article) {
        this.title = title;
        this.allowMultipleSelection = allowMultipleSelection;
        this.allowTextAnswer = allowTextAnswer;
        this.deadLine = deadLine;
        this.article = article;
    }

    public void addVoteItem(VoteItem voteItem) {
        this.voteItems.add(voteItem);
    }

    public boolean isClosed() {
        return this.deadLine != null && LocalDateTime.now().isAfter(this.deadLine);
    }

    public void delete() {
        this.markAsActive();
        this.voteItems.forEach(VoteItem::delete);
        this.voteResponses.forEach(VoteAnswer::delete);
    }

    public void associateArticle(Article article) {
        this.article = article;
    }

    public void disassociateArticle() {
        this.article = null;
    }
}
