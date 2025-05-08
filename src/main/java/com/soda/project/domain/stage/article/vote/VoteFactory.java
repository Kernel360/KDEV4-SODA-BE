package com.soda.project.domain.stage.article.vote;

import com.soda.member.domain.member.Member;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.interfaces.stage.article.vote.VoteSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteFactory {

    private final VoteItemFactory voteItemFactory;

    public Vote createVoteWithItems(Article article, String title, boolean allowMultipleSelection,
                                    boolean allowTextAnswer, LocalDateTime deadLine, List<String> itemTexts) {
        log.debug("[VoteFactory] Vote 엔티티 생성 시작: title={}, articleId={}", title, article.getId());

        // 1. Vote 엔티티 생성 (엔티티의 정적 팩토리 메서드 사용)
        Vote vote = Vote.create(title, allowMultipleSelection, allowTextAnswer, deadLine, article);
        log.debug("[VoteFactory] Vote 엔티티 생성 완료 (저장 전)");

        // 2. 항목 투표이고, 항목 텍스트가 제공된 경우 VoteItem 생성 및 연결
        if (!allowTextAnswer && !CollectionUtils.isEmpty(itemTexts)) {
            List<VoteItem> createdItems = voteItemFactory.createVoteItems(vote, itemTexts);
            if (createdItems != null) {
                vote.getVoteItems().clear(); // 기존 collection 초기화
                vote.getVoteItems().addAll(createdItems);
            }
        }

        return vote;
    }

    public VoteAnswer createAnswerWithItems(Vote vote, Member submitter, VoteSubmitRequest request, List<VoteItem> selectedItems) {
        log.debug("VoteAnswerFactory: Answer 생성 시작 (Entity.create 호출) - voteId={}, submitterId={}", vote.getId(), submitter.getId());

        // VoteAnswer 엔티티의 정적 팩토리 메서드 호출
        VoteAnswer voteAnswer = VoteAnswer.create(
                vote,
                submitter,
                request.getTextAnswer(),
                selectedItems
        );

        log.debug("VoteAnswerFactory: Answer 객체 생성 완료 (저장 전), Item 개수: {}", voteAnswer.getSelectedItems().size());
        return voteAnswer;
    }
}
