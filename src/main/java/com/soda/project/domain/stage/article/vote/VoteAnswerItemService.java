package com.soda.project.domain.stage.article.vote;

import com.soda.project.domain.stage.article.error.VoteErrorCode;
import com.soda.project.infrastructure.VoteAnswerItemRepository;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoteAnswerItemService {

    private final VoteAnswerItemRepository voteAnswerItemRepository;

    @Transactional
    public void createAndSaveVoteAnswerItems (VoteAnswer voteAnswer, List<VoteItem> selectedItems) {
        if (CollectionUtils.isEmpty(selectedItems)) {
            log.debug("저장할 VoteAnswerItem 이 없습니다. VoteAnswer ID: {}", voteAnswer.getId());
            return;
        }

        Vote vote = voteAnswer.getVote();

        // 단일 선택 투표인데 여러 항목을 저장하려는 경우 방지
        if (!vote.isAllowMultipleSelection() && selectedItems.size() > 1) {
            log.warn("단일 선택 투표(Vote ID: {})에 여러 항목({}) 저장을 시도했습니다. VoteAnswer ID: {}",
                    vote.getId(), selectedItems.size(), voteAnswer.getId());
            throw new GeneralException(VoteErrorCode.VOTE_MULTIPLE_SELECTION_NOT_ALLOWED);
        }

        List<VoteAnswerItem> answerItemsToSave = new ArrayList<>();
        for (VoteItem item : selectedItems) {
            VoteAnswerItem answerItem = VoteAnswerItem.builder()
                    .voteResponse(voteAnswer)
                    .voteItem(item)
                    .build();
            answerItemsToSave.add(answerItem);

            voteAnswer.addSelectedItem(answerItem);
        }

        voteAnswerItemRepository.saveAll(answerItemsToSave);
        log.info("VoteAnswerItem {}개 저장 완료 (VoteAnswerItemService). Answer ID: {}, Item IDs: {}",
                answerItemsToSave.size(), voteAnswer.getId(), selectedItems.stream().map(VoteItem::getId).collect(Collectors.toList()));
    }

    // 특정 투표의 특정 항목별 득표 수
    public Map<Long, Long> countItemsByVote(Long voteId) {
        Map<Long, Long> counts = voteAnswerItemRepository.countItemGroupByVoteItemId(voteId);
        log.debug("Vote ID {} 항목별 활성 답변 집계 (VoteAnswerItemService): {}", voteId, counts);
        return counts;
    }

}
