package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByArticle_IdAndIsDeletedFalse(Long articleId);

    Optional<Vote> findByIdAndIsDeletedFalse(Long voteId);
}
