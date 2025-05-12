package com.soda.project.domain.stage.article.vote;

import com.soda.member.domain.member.Member;
import com.soda.project.interfaces.stage.article.vote.VoteSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteAnswerFactory {
    private final VoteAnswerProvider voteAnswerProvider;

    public VoteAnswer createAnswerWithItems(Vote vote, Member submitter, VoteSubmitRequest request, List<VoteItem> selectedItems) {
        log.debug("VoteAnswerFactory: Answer 생성 시작 (Entity.create 호출) - voteId={}, submitterId={}", vote.getId(), submitter.getId());

        // VoteAnswer 엔티티의 정적 팩토리 메서드 호출
        VoteAnswer voteAnswer = VoteAnswer.create(
                vote,
                submitter,
                request.getTextAnswer(),
                selectedItems
        );

        VoteAnswer savedAnswer = voteAnswerProvider.storeAnswerWithItems(voteAnswer);
        log.info("[VoteFactory] Answer 및 연관 Items 저장 완료 - answerId={}", savedAnswer.getId());

        return savedAnswer;
    }
}
