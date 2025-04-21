package com.soda.article.repository;

import com.soda.article.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    boolean existsByArticleIdAndIsDeletedFalse(Long articleId);

}
