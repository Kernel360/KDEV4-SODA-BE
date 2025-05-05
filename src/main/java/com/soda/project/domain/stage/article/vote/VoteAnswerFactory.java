package com.soda.project.domain.stage.article.vote;

import com.soda.member.entity.Member;
import com.soda.project.interfaces.dto.stage.article.vote.VoteSubmitRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VoteAnswerFactory {

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
