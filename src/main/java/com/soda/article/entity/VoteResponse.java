package com.soda.article.entity;

import com.soda.article.error.ArticleErrorCode;
import com.soda.common.BaseEntity;
import com.soda.global.response.GeneralException;
import com.soda.member.entity.Member;
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
@Table(uniqueConstraints = {
        // 한 명의 사용자는 하나의 투표에 한 번만 응답 가능
        @UniqueConstraint(columnNames = {"vote_id", "member_id"})
})
public class VoteResponse extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    @OneToMany(mappedBy = "voteResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteResponseItem> selectedItems = new ArrayList<>();

    @Builder
    public VoteResponse(Vote vote, Member member, String textAnswer) {
        // 투표 마감 확인
        if (vote.isClosed()) {
            throw new GeneralException(ArticleErrorCode.VOTE_ALREADY_CLOSED);
        }

        // text 답변 허용 여부 확인
        if (textAnswer != null && !vote.isAllowTextAnswer()) {
            throw new GeneralException(ArticleErrorCode.VOTE_TEXT_ANSWER_NOT_ALLOWED);
        }

        this.vote = vote;
        this.member = member;
        this.textAnswer = textAnswer;
    }

    // VoteResponseItem 추가
    public void addSelectedItem(VoteResponseItem item) {
        if (this.selectedItems == null) {
            this.selectedItems = new ArrayList<>();
        }

        // 단일 선택 검증
        if (!this.vote.isAllowMultipleSelection() && !this.selectedItems.isEmpty()) {
            throw new GeneralException(ArticleErrorCode.VOTE_MULTIPLE_SELECTION_NOT_ALLOWED);
        }

        this.selectedItems.add(item);
    }

    public void delete() {
        this.markAsDeleted();
        if (this.selectedItems != null) {
            this.selectedItems.forEach(VoteResponseItem::delete);
        }
    }
}
