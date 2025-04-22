package com.soda.article.repository;

import com.soda.article.entity.VoteAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteAnswerRepository extends JpaRepository<VoteAnswer, Long> {
    boolean existsByVote_IdAndMember_Id(Long voteId, Long userId);
}
