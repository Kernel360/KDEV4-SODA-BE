package com.soda.project.domain.stage.article.vote;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.article.error.VoteErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteItemFactory {

    private final VoteItemProvider voteItemProvider;

    // VoteItem List 생성 및 저장
    public List<VoteItem> createVoteItems(Vote vote, List<String> itemTexts) {
        if (CollectionUtils.isEmpty(itemTexts)) {
            log.debug("생성할 투표 항목 텍스트 목록이 비어있습니다. voteId={}", vote.getId());
            return Collections.emptyList();
        }

        List<VoteItem> voteItemsToSave = itemTexts.stream()
                .map(text -> VoteItem.create(text, vote))
                .toList();
        List<VoteItem> savedVoteItems = voteItemProvider.storeAll(voteItemsToSave);

        log.info("[VoteItemFactory] VoteItem 생성 및 저장 완료: voteId={}, savedCount={}", vote.getId(), savedVoteItems.size());
        return savedVoteItems;
    }

    // VoteItem 생성 및 저장
    public VoteItem createItem(Vote vote, String itemText) {
        VoteItem voteItem = VoteItem.create(itemText, vote);
        VoteItem savedItem = voteItemProvider.store(voteItem);

        log.info("[VoteItemFactory] 단일 VoteItem 생성 및 저장 완료. Item ID: {}", savedItem.getId());
        return savedItem;
    }

    // VoteItem 조회
    public List<VoteItem> findItemsByIds(List<Long> itemIds) {
        if (CollectionUtils.isEmpty(itemIds)) {
            return new ArrayList<>();
        }
        List<VoteItem> foundItems = voteItemProvider.findAllById(itemIds);

        if (foundItems.size() != itemIds.size()) {
            log.warn("요청된 VoteItem ID 목록에 존재하지 않는 ID가 포함되어 있습니다. Requested: {}, Found count: {}",
                    itemIds, foundItems.size());
            throw new GeneralException(VoteErrorCode.INVALID_VOTE_ITEM);
        }
        log.debug("VoteItem {}개 조회 완료 (VoteItemService). IDs: {}", foundItems.size(), itemIds);

        return foundItems;
    }
}
