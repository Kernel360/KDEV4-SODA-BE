package com.soda.project.infrastructure.stage.article.vote;

import com.soda.project.domain.stage.article.vote.Vote;
import com.soda.project.domain.stage.article.vote.VoteAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
    boolean existsByVote_IdAndMember_Id(Long voteId, Long userId);

    int countByVote(Vote vote);

    List<VoteAnswer> findByVote(Vote vote);

    int countByVote_IdAndIsDeletedFalse(Long voteId);

    List<VoteAnswer> findByVote_IdAndIsDeletedFalse(Long voteId);
}
