package com.soda.project.domain.stage.article.vote;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.domain.stage.article.vote.dto.*;
import com.soda.project.infrastructure.VoteRepository;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteItemService voteItemService;
    private final VoteAnswerService voteAnswerService;
    private final VoteAnswerItemService voteAnswerItemService;

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
        boolean textAllowed = request.getAllowTextAnswer();

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
            // Set을 이용하여 중복 제거 후 크기 비교
            Set<String> distinctItems = new HashSet<>(itemTexts);
            if (distinctItems.size() != itemTexts.size()) {
                log.warn("유효성 검증 실패: 투표 항목 요청에 중복된 내용이 있습니다. Items: {}", itemTexts);
                throw new GeneralException(VoteErrorCode.VOTE_DUPLICATE_ITEM_TEXT);
            }
            log.debug("투표 항목 중복 검증 통과. {}개 항목 유효.", distinctItems.size());
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

    @Transactional
    public VoteSubmitResponse processVoteSubmit(Long voteId, Long userId, VoteSubmitRequest request) {
        log.info("[투표 처리 시작(Response 반환)] Vote ID: {}, User ID: {}", voteId, userId);

        // 1. 투표(Vote) 정보 조회 및 유효성 검증
        Vote vote = voteRepository.findByIdAndIsDeletedFalse(voteId)
                .orElseThrow(() -> {
                    log.warn("[투표 처리 실패] Vote ID {} 를 찾을 수 없거나 삭제되었습니다.", voteId);
                    return new GeneralException(VoteErrorCode.VOTE_NOT_FOUND);
                });

        // 2. 중복 투표 확인
        if (voteAnswerService.hasUserVoted(voteId, userId)) {
            log.warn("[투표 조율 실패] User ID {} 는 이미 Vote ID {} 에 투표했습니다.", userId, voteId);
            throw new GeneralException(VoteErrorCode.ALREADY_VOTED);
        }

        // 3. 투표 입력값 유효성 검증
        validateVoteInput(vote, request);
        log.debug("[투표 조율] 입력값 유효성 검증 통과.");

        // 4. 투표 답변 생성 및 저장
        VoteAnswer savedAnswer = voteAnswerService.createAndSaveVoteAnswer(vote, userId, request.getTextAnswer());
        log.info("[투표 조율] VoteAnswer 저장 위임 완료. Answer ID: {}", savedAnswer.getId());

        // 5. 투표 답변 항목 생성 및 저장
        List<Long> savedItemIds = Collections.emptyList();
        if (!vote.isAllowTextAnswer()) {
            List<VoteItem> selectedItems = voteItemService.findVoteItemsByIds(request.getSelectedItemIds());

            voteAnswerItemService.createAndSaveVoteAnswerItems(savedAnswer, selectedItems);
            savedItemIds = request.getSelectedItemIds();
            log.info("[투표 조율] VoteAnswerItem 저장 위임 완료. Answer ID: {}, Item IDs: {}",
                    savedAnswer.getId(), savedItemIds);
        }

        VoteSubmitResponse response = VoteSubmitResponse.builder()
                .voteId(voteId)
                .voterId(userId)
                .voteAnswerId(savedAnswer.getId())
                .selectedItemIds(savedItemIds) // 텍스트 투표 시 비어있음
                .build();

        log.info("[투표 조율 성공(Response 반환)] Vote ID: {}, User ID: {}, Answer ID: {}",
                response.getVoteId(), response.getVoterId(), response.getVoteAnswerId());

        return response;
    }

    private void validateVoteInput(Vote vote, VoteSubmitRequest request) {
        boolean hasTextAnswer = StringUtils.hasText(request.getTextAnswer());
        boolean hasSelectedItems = !CollectionUtils.isEmpty(request.getSelectedItemIds());

        if (vote.isAllowTextAnswer()) {
            if (!hasTextAnswer) {
                log.warn("입력 검증 실패: 텍스트 투표에는 답변이 필요합니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }

            log.debug("Vote ID {} 텍스트 입력값 검증 통과", vote.getId());
        } else {
            if (!hasSelectedItems) {
                log.warn("입력 검증 실패: 항목 선택 투표에는 항목 선택이 필요합니다. Vote ID: {}", vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
            }

            // 선택한 항목들이 이 투표에 속하는지 검증
            validateSelectedItemsBelongToVote(vote, request.getSelectedItemIds());

            // 단일/다중 선택 제약 조건 검증
            if (!vote.isAllowMultipleSelection()) {
                // 단일 선택만 허용
                if (request.getSelectedItemIds().size() != 1) {
                    log.warn("입력 검증 실패: 단일 선택 투표에 여러 항목({})이 선택되었습니다. Vote ID: {}", request.getSelectedItemIds().size(), vote.getId());
                    throw new GeneralException(VoteErrorCode.INVALID_VOTE_INPUT);
                }
                log.debug("Vote ID {} 단일 선택 입력값 검증 통과", vote.getId());
            } else {
                // 다중 선택 허용
                log.debug("Vote ID {} 다중 선택 입력값 검증 통과", vote.getId());
            }
        }
    }

    private void validateSelectedItemsBelongToVote(Vote vote, List<Long> selectedItemIds) {
        Set<Long> actualItemIds = vote.getVoteItems().stream()
                .map(VoteItem::getId)
                .collect(Collectors.toSet());

        if (actualItemIds.isEmpty() && !selectedItemIds.isEmpty()) {
            log.error("Vote ID {} 에는 VoteItem이 없는데 항목 선택이 요청되었습니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.VOTE_ITEM_NOT_FOUND);
        }

        for (Long selectedId : selectedItemIds) {
            if (!actualItemIds.contains(selectedId)) {
                log.warn("[항목 검증 실패] 선택된 항목 ID {} 는 Vote ID {} 에 속하지 않습니다.", selectedId, vote.getId());
                throw new GeneralException(VoteErrorCode.INVALID_VOTE_ITEM);
            }
        }
        log.debug("Vote ID {} 선택 항목 ID {} 유효성 검증 통과", vote.getId(), selectedItemIds);
    }

    @Transactional
    public VoteItemAddResponse addVoteItemToVote(Vote vote, String itemText) {
        log.info("[항목 추가 시작(VoteService)] Vote ID: {}, Item Text: '{}'", vote.getId(), itemText);

        // 1. 투표 상태 검증
        validateVoteForAddingItem(vote);

        // 2. 중복 항목 검증
        checkDuplicateVoteItem(vote, itemText);

        // 3. VoteItemService 에 생성 및 저장 위임
        VoteItem savedItem = voteItemService.createAndSaveVoteItem(vote, itemText);

        // 4. Vote 엔티티의 컬렉션에도 추가
        vote.addVoteItem(savedItem);

        log.info("[항목 추가 완료(VoteService)] Vote ID: {}, New Item ID: {}", vote.getId(), savedItem.getId());

        // 5. 응답 DTO 생성 및 반환
        return VoteItemAddResponse.from(savedItem);
    }

    private void checkDuplicateVoteItem(Vote vote, String itemText) {
        boolean isDuplicate = vote.getVoteItems().stream()
            .anyMatch(item -> !item.getIsDeleted() && item.getText().equals(itemText));
        if (isDuplicate) {
            log.warn("항목 추가 실패: Vote ID {} 에 이미 '{}' 항목이 존재합니다.", vote.getId(), itemText);
            throw new GeneralException(VoteErrorCode.VOTE_DUPLICATE_ITEM_TEXT);
        }
        log.debug("Vote ID {} 에 항목 '{}' 중복 없음 확인", vote.getId(), itemText);
    }

    private void validateVoteForAddingItem(Vote vote) {
        // 1. 마감 여부 확인
        if (vote.isClosed()) {
            log.warn("항목 추가 실패: Vote ID {} 는 이미 마감되었습니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.VOTE_ALREADY_CLOSED);
        }
        // 2. 텍스트 전용 투표인지 확인 (항목 추가 불가)
        if (vote.isAllowTextAnswer()) {
            log.warn("항목 추가 실패: Vote ID {} 는 텍스트 답변 전용 투표입니다.", vote.getId());
            throw new GeneralException(VoteErrorCode.CANNOT_ADD_ITEM_TO_TEXT_VOTE);
        }
        log.debug("항목 추가를 위한 Vote ID {} 상태 검증 통과", vote.getId());
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