package com.soda.project.domain.stage.article.vote;

import com.soda.member.entity.Member;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.infrastructure.VoteRepository;
import com.soda.global.response.GeneralException;
import com.soda.project.interfaces.dto.stage.article.vote.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final VoteProvider voteProvider;
    private final VoteAnswerFactory voteAnswerFactory;
    private final VoteAnswerProvider voteAnswerProvider;
    private final VoteItemService voteItemService;
    private final VoteAnswerService voteAnswerService;
    private final VoteAnswerItemService voteAnswerItemService;

    public VoteCreateResponse createVoteAndItems(Article article, String title, boolean allowMultipleSelection,
                                                 boolean allowTextAnswer, LocalDateTime deadLine, List<String> itemTexts) {
        log.info("Vote 생성 및 저장 시작 (VoteService): articleId={}", article.getId());

        Vote vote = Vote.create(title, allowMultipleSelection, allowTextAnswer, deadLine, article);
        Vote savedVote = voteProvider.store(vote);

        if (!allowTextAnswer && !CollectionUtils.isEmpty(itemTexts)) {
            List<VoteItem> createdItems = voteItemService.createVoteItems(savedVote, itemTexts);
            if (createdItems != null) {
                savedVote.getVoteItems().clear(); // 기존 collection 초기화
                savedVote.getVoteItems().addAll(createdItems);
            }
        }
        log.info("Vote 생성 최종 완료 (VoteService): voteId={}, articleId={}", savedVote.getId(), article.getId());

        // 응답 DTO 생성 및 반환
        return VoteCreateResponse.from(savedVote);
    }

    public boolean doesActiveVoteExistForArticle(Long articleId) {
        return voteProvider.existsByArticleIdAndIsDeletedFalse(articleId);
    }

    public Vote findVoteById(Long voteId) {
        return voteProvider.findById(voteId)
                .orElseThrow(() -> new GeneralException(VoteErrorCode.VOTE_NOT_FOUND));
    }

    @Transactional
    public VoteAnswer submitAnswer(Vote vote, Member submitter, VoteSubmitRequest request, List<VoteItem> selectedItems) {
        VoteAnswer voteAnswer = voteAnswerFactory.createAnswerWithItems(vote, submitter, request, selectedItems);
        log.debug("VoteService: VoteAnswer 객체 생성 완료 (저장 전)");

        VoteAnswer savedAnswer = voteAnswerProvider.storeAnswerWithItems(voteAnswer);
        log.info("VoteService: VoteAnswer 저장 완료 - answerId={}", savedAnswer.getId());

        return savedAnswer;
    }

    @Transactional
    public VoteItemAddResponse addVoteItemToVote(Vote vote, String itemText) {
        VoteItem savedItem = voteItemService.createAndSaveVoteItem(vote, itemText);
        vote.addVoteItem(savedItem);
        return VoteItemAddResponse.from(savedItem);
    }

    public VoteResultResponse getVoteResultData(Vote vote) {
        log.debug("[결과 집계 시작(VoteService)] Vote ID: {}", vote.getId());

        // 1. 총 참여자 수 조회 (VoteAnswerService 사용)
        int totalParticipants = voteAnswerService.countAnswers(vote);
        log.debug("Vote ID {} 총 참여자 수: {}", vote.getId(), totalParticipants);

        Map<Long, Long> itemCounts = Collections.emptyMap();
        List<String> textAnswers = Collections.emptyList();

        // 2. 투표 유형에 따라 결과 집계 (각 서비스 사용)
        if (!vote.isAllowTextAnswer()) { // 항목 투표
            // 항목별 득표 수 집계 (VoteAnswerItemService 사용)
            itemCounts = voteAnswerItemService.countItemsByVote(vote.getId());
            log.debug("Vote ID {} 항목별 집계 결과: {}", vote.getId(), itemCounts);
        } else { // 텍스트 투표
            // 텍스트 답변 목록 조회
            textAnswers = voteAnswerService.findTextAnswers(vote);
            log.debug("Vote ID {} 텍스트 답변 {}개 조회", vote.getId(), textAnswers.size());
        }

        return VoteResultResponse.from(vote, totalParticipants, itemCounts, textAnswers);
    }
}