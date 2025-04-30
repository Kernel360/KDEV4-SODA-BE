package com.soda.project.article.repository;

import com.soda.project.domain.stage.article.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByArticleIdAndIsDeletedFalse(Long articleId);

    Optional<Vote> findByIdAndIsDeletedFalse(Long voteId);
}
