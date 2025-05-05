package com.soda.project.domain.stage.article.vote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class VoteItemFactory {

    /**
     * VoteItem 엔티티 리스트 생성
     */
    public List<VoteItem> createItems(Vote vote, List<String> itemTexts) {
        if (CollectionUtils.isEmpty(itemTexts)) {
            log.debug("생성할 투표 항목 텍스트 목록이 비어있습니다. voteId={}", vote.getId());
            return Collections.emptyList();
        }
        log.debug("VoteItemFactory: VoteItem {}개 생성 시작 (저장 전)", itemTexts.size());
        return itemTexts.stream()
                .map(text -> VoteItem.create(text, vote))
                .toList();
    }

    /**
     * 단일 VoteItem 엔티티를 생성
     */
    public VoteItem createItem(Vote vote, String itemText) {
        log.debug("VoteItemFactory: 단일 VoteItem 생성 시작 (저장 전) - text='{}'", itemText);
        return VoteItem.create(itemText, vote);
    }
}
