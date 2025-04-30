package com.soda.project.article.service;

import com.soda.project.domain.stage.article.Vote;
import com.soda.project.domain.stage.article.VoteItem;
import com.soda.project.article.error.VoteErrorCode;
import com.soda.project.article.repository.VoteItemRepository;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteItemService {

    private final VoteItemRepository voteItemRepository;

    /**
     * Vote 항목들 저장
     * @param vote 항목들 저장할 Vote
     * @param itemTexts 생성할 항목들 텍스트
     * @return VoteItem 엔티티 리스트
     */
    @Transactional
    public List<VoteItem> createVoteItems(Vote vote, List<String> itemTexts) {
        if (CollectionUtils.isEmpty(itemTexts)) {
            log.debug("생성할 투표 항목 텍스트 목록이 비어있습니다. voteId={}", vote.getId());
            return Collections.emptyList();
        }

        log.info("VoteItem 생성 시작: voteId={}, itemCount={}", vote.getId(), itemTexts.size());

        // 1. 각 항목에 대한 VoteItem 생성
        List<VoteItem> voteItems = itemTexts.stream()
                .map(text -> VoteItem.builder()
                        .text(text)
                        .vote(vote)
                        .build())
                .toList();

        // 2. DB에 저장
        List<VoteItem> savedVoteItems = voteItemRepository.saveAll(voteItems);

        log.info("VoteItem 생성 및 저장 완료: voteId={}, savedCount={}", vote.getId(), savedVoteItems.size());
        return savedVoteItems;
    }

    public List<VoteItem> findVoteItemsByIds(List<Long> itemIds) {
        if (CollectionUtils.isEmpty(itemIds)) {
            return new ArrayList<>();
        }
        List<VoteItem> foundItems = voteItemRepository.findAllById(itemIds);

        if (foundItems.size() != itemIds.size()) {
            log.warn("요청된 VoteItem ID 목록에 존재하지 않는 ID가 포함되어 있습니다. Requested: {}, Found count: {}",
                    itemIds, foundItems.size());
            throw new GeneralException(VoteErrorCode.INVALID_VOTE_ITEM);
        }
        log.debug("VoteItem {}개 조회 완료 (VoteItemService). IDs: {}", foundItems.size(), itemIds);

        return foundItems;
    }

    @Transactional
    public VoteItem createAndSaveVoteItem (Vote vote, String itemText) {
        VoteItem voteItem = VoteItem.builder()
                .vote(vote)
                .text(itemText)
                .build();
        VoteItem savedItem = voteItemRepository.save(voteItem);

        log.info("단일 VoteItem 생성 및 저장 완료 (VoteItemService). Vote ID: {}, Item ID: {}, Text: {}",
                vote.getId(), savedItem.getId(), itemText);
        return savedItem;
    }

}