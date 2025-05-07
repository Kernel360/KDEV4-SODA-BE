package com.soda.project.domain.stage.article.vote;

import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.common.BaseEntity;
import com.soda.global.response.GeneralException;
import com.soda.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(uniqueConstraints = {
        // 한 명의 사용자는 하나의 투표에 한 번만 응답 가능
        @UniqueConstraint(columnNames = {"vote_id", "member_id"})
})
public class VoteAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(columnDefinition = "TEXT")
    private String textAnswer;

    @OneToMany(mappedBy = "voteResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteAnswerItem> selectedItems = new ArrayList<>();

    @Builder
    public VoteAnswer(Vote vote, Member member, String textAnswer) {
        // 투표 마감 확인
        if (vote.isClosed()) {
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        }

        // text 답변 허용 여부 확인
        if (textAnswer != null && !vote.isAllowTextAnswer()) {
            throw new GeneralException(VoteErrorCode.VOTE_TEXT_ANSWER_NOT_ALLOWED);
        }

        this.vote = vote;
        this.member = member;
        this.textAnswer = textAnswer;
    }

    // VoteResponseItem 추가
    public void addSelectedItem(VoteAnswerItem item) {
        if (this.selectedItems == null) {
            this.selectedItems = new ArrayList<>();
        }

        // 단일 선택 검증
        if (!this.vote.isAllowMultipleSelection() && !this.selectedItems.isEmpty()) {
            throw new GeneralException(VoteErrorCode.VOTE_MULTIPLE_SELECTION_NOT_ALLOWED);
        }

        this.selectedItems.add(item);
    }

    public void delete() {
        this.markAsDeleted();
        if (this.selectedItems != null) {
            this.selectedItems.forEach(VoteAnswerItem::delete);
        }
    }

    public static VoteAnswer create(Vote vote, Member member, String textAnswer, List<VoteItem> selectedItems) {
        validateVoteStateAndInput(vote, textAnswer, selectedItems);

        VoteAnswer voteAnswer = VoteAnswer.builder()
                .vote(vote)
                .member(member)
                .textAnswer(textAnswer)
                .build();

        if (!vote.isAllowTextAnswer() && !CollectionUtils.isEmpty(selectedItems)) {
            for (VoteItem item : selectedItems) {
                VoteAnswerItem answerItem = VoteAnswerItem.create(voteAnswer, item);
                voteAnswer.selectedItems.add(answerItem);
            }
        }

        return voteAnswer;
    }

    private static void validateVoteStateAndInput(Vote vote, String textAnswer, List<VoteItem> selectedItems) {
        // 1. 투표 마감 확인
        if (vote.isClosed()) {
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        }

        boolean hasText = StringUtils.hasText(textAnswer);
        boolean hasItems = !CollectionUtils.isEmpty(selectedItems);

        // 2. 투표 유형에 따른 입력값 검증
        if (vote.isAllowTextAnswer()) { // 텍스트 답변 허용 투표
            if (!hasText) { // 텍스트 답변 필수
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            if (hasItems) { // 항목 선택 불가
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
        } else { // 항목 선택 투표
            if (!hasItems) { // 항목 선택 필수
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            if (hasText) { // 텍스트 답변 불가
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }
            // 단일 선택 검증
            if (!vote.isAllowMultipleSelection() && selectedItems.size() > 1) {
                throw new GeneralException(VoteErrorCode.VOTE_MULTIPLE_SELECTION_NOT_ALLOWED);
            }
        }
    }
}
