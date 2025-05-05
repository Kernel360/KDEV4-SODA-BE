package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.vote.VoteItem;
import com.soda.project.domain.stage.article.vote.VoteItemProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoteItemProviderImpl implements VoteItemProvider {
    private final VoteItemRepository voteItemRepository;

    @Override
    public List<VoteItem> storeAll(List<VoteItem> voteItems) {
        if (voteItems == null || voteItems.isEmpty()) {
            return List.of();
        }
        return voteItemRepository.saveAll(voteItems);
    }

    @Override
    public List<VoteItem> findAllById(List<Long> itemIds) {
        return voteItemRepository.findAllById(itemIds);
    }
}
