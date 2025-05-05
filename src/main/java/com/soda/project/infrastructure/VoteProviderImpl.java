package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.vote.Vote;
import com.soda.project.domain.stage.article.vote.VoteProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteProviderImpl implements VoteProvider {
    private final VoteRepository voteRepository;

    @Override
    public Vote store(Vote vote) {
        return voteRepository.save(vote);
    }

    @Override
    public boolean existsByArticleIdAndIsDeletedFalse(Long articleId) {
        return voteRepository.existsByArticleIdAndIsDeletedFalse(articleId);
    }
}
