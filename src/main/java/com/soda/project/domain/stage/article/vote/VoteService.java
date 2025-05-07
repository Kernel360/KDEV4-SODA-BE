package com.soda.project.domain.stage.article.vote;

import com.soda.member.domain.Member;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.interfaces.stage.article.vote.VoteCreateResponse;
import com.soda.project.interfaces.stage.article.vote.VoteResultResponse;
import com.soda.project.interfaces.stage.article.vote.VoteSubmitRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteProvider voteProvider;
    private final VoteAnswerFactory voteAnswerFactory;
    private final VoteFactory voteFactory;
    private final VoteResultCalculator voteResultCalculator;
    private final VoteItemFactory voteItemFactory;

    public VoteCreateResponse createVoteAndItems(Article article, String title, boolean allowMultipleSelection,
                                                 boolean allowTextAnswer, LocalDateTime deadLine, List<String> itemTexts) {
        log.info("Vote 생성 및 저장 시작 (VoteService): articleId={}", article.getId());

        Vote vote = voteFactory.createVoteWithItems(article, title, allowMultipleSelection, allowTextAnswer, deadLine, itemTexts);
        Vote savedVote = voteProvider.store(vote);
        log.info("[VoteService] Vote 및 연관 VoteItem 저장 완료: voteId={}", savedVote.getId());

        return VoteCreateResponse.from(savedVote);
    }

    public boolean doesActiveVoteExistForArticle(Long articleId) {
        return voteProvider.existsByArticleIdAndIsDeletedFalse(articleId);
    }

    public VoteAnswer submitAnswer(Vote vote, Member submitter, VoteSubmitRequest request, List<VoteItem> selectedItems) {
        return voteAnswerFactory.createAnswerWithItems(vote, submitter, request, selectedItems);
    }

    public VoteResultResponse getVoteResultData(Vote vote) {
        return voteResultCalculator.calculateResults(vote);
    }

    public List<VoteItem> findVoteItemsByIds(List<Long> itemIds) {
        return voteItemFactory.findItemsByIds(itemIds);
    }

    public VoteItem addVoteItem(Vote vote, String itemText) {
        return voteItemFactory.createItem(vote, itemText);
    }

}