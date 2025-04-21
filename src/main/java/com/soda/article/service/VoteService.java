package com.soda.article.service;

import com.soda.article.dto.article.VoteCreateRequest;
import com.soda.article.dto.article.VoteCreateResponse;
import com.soda.article.entity.Article;
import com.soda.article.entity.Vote;
import com.soda.article.entity.VoteItem;
import com.soda.article.error.VoteErrorCode;
import com.soda.article.repository.VoteRepository;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteItemService voteItemService;

    @Transactional
    public VoteCreateResponse createVote(Article article, VoteCreateRequest request) {
        log.info("Vote 생성 시작 (VoteService): articleId={}", article.getId());

        validateVoteRequest(request);

        Vote vote = Vote.builder()
                .title(request.getTitle())
                .allowMultipleSelection(request.getAllowMultipleSelection())
                .allowTextAnswer(request.getAllowTextAnswer())
                .deadLine(request.getDeadLine())
                .article(article)
                .build();

        // vote 저장
        Vote savedVote = voteRepository.save(vote);
        log.debug("Vote 엔티티 저장 완료 (VoteService) : voteId = {}", savedVote.getId());

        if (!CollectionUtils.isEmpty(request.getVoteItems())) {
            List<VoteItem> createdItems = voteItemService.createVoteItems(savedVote, request.getVoteItems());
            // 메모리 상태 동기화
            if (createdItems != null) {
                createdItems.forEach(savedVote::addVoteItem);
            }
        }

        log.info("Vote 생성 최종 완료 (VoteService): voteId={}, articleId={}", savedVote.getId(), article.getId());

        // 응답 DTO 생성 및 반환
        return VoteCreateResponse.from(savedVote);
    }

    private void validateVoteRequest(VoteCreateRequest request) {

    }

    public boolean doesActiveVoteExistForArticle(Long articleId) {
        return voteRepository.existsByArticleIdAndIsDeletedFalse(articleId);
    }

    public Vote findVoteById(Long voteId) {
        return voteRepository.findById(voteId)
                .orElseThrow(() -> new GeneralException(VoteErrorCode.VOTE_NOT_FOUND));
    }
}