package com.soda.article.repository;

import com.soda.article.entity.Vote;
import com.soda.article.entity.VoteAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
    boolean existsByVote_IdAndMember_Id(Long voteId, Long userId);

    int countByVote(Vote vote);

    List<VoteAnswer> findByVote(Vote vote);
}
