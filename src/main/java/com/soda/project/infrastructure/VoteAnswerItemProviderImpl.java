package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.vote.VoteAnswerItemProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class VoteAnswerItemProviderImpl implements VoteAnswerItemProvider {
    private VoteAnswerItemRepository voteAnswerItemRepository;

    @Override
    public Map<Long, Long> countItemsByVote(Long voteId) {
        return voteAnswerItemRepository.countItemGroupByVoteItemId(voteId);
    }
}
