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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        boolean hasItems = !CollectionUtils.isEmpty(request.getVoteItems());
        boolean textAllowed = request.getAllowTextAnswer(); // DTO에서 @NotNull 보장

        // 1. 항목 선택 투표 시 항목 필수 검증
        if (!textAllowed && !hasItems) {
            log.warn("유효성 검증 실패: 항목 선택 투표에는 항목이 필수입니다.");
            throw new GeneralException(VoteErrorCode.VOTE_ITEM_REQUIRED);
        }

        // 2. 텍스트 답변 투표 시 항목 불가 검증
        if (textAllowed && hasItems) {
            log.warn("유효성 검증 실패: 텍스트 답변 투표에는 항목을 포함할 수 없습니다.");
            throw new GeneralException(VoteErrorCode.VOTE_CANNOT_HAVE_BOTH_ITEMS_AND_TEXT);
        }

        // 3. 항목 중복 검사 (항목이 있는 경우에만)
        if (hasItems) {
            List<String> itemTexts = request.getVoteItems();
            Set<String> distinctItems = new HashSet<>(itemTexts);
            if (distinctItems.size() != itemTexts.size()) {
                log.warn("유효성 검증 실패: 투표 항목에 중복된 내용이 있습니다.");
                throw new GeneralException(VoteErrorCode.VOTE_DUPLICATE_ITEM_TEXT);
            }
        }

        log.debug("투표 생성 요청 데이터 유효성 검증 통과");
    }

    public boolean doesActiveVoteExistForArticle(Long articleId) {
        return voteRepository.existsByArticleIdAndIsDeletedFalse(articleId);
    }

    public Vote findVoteById(Long voteId) {
        return voteRepository.findById(voteId)
                .orElseThrow(() -> new GeneralException(VoteErrorCode.VOTE_NOT_FOUND));
    }
}