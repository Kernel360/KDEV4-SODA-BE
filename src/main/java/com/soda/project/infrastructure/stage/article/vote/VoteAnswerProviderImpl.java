package com.soda.project.infrastructure.stage.article.vote;

import com.soda.project.domain.stage.article.vote.VoteAnswer;
import com.soda.project.domain.stage.article.vote.VoteAnswerProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VoteAnswerProviderImpl implements VoteAnswerProvider {
    private final VoteAnswerRepository voteAnswerRepository;

    @Override
    public VoteAnswer storeAnswerWithItems(VoteAnswer voteAnswer) {
        return voteAnswerRepository.save(voteAnswer);
    }

    @Override
    public boolean hasUserVoted(Long voteId, Long userId) {
        return voteAnswerRepository.existsByVote_IdAndMember_Id(voteId, userId);
    }

    @Override
    public int countAnswersByVote(Long voteId) {
        return voteAnswerRepository.countByVote_IdAndIsDeletedFalse(voteId);
    }

    @Override
    public List<String> findTextAnswersByVote(Long voteId) {
        List<VoteAnswer> answers = voteAnswerRepository.findByVote_IdAndIsDeletedFalse(voteId);
        return answers.stream()
                .map(VoteAnswer::getTextAnswer)
                .filter(StringUtils::hasText)
                .toList();
    }

    @Override
    public boolean existsByVote_IdAndMember_Id(Long voteId, Long userId) {
        return voteAnswerRepository.existsByVote_IdAndMember_Id(voteId, userId);
    }
}
