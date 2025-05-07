package com.soda.project.infrastructure.stage.article.vote;

import com.soda.project.domain.stage.article.vote.VoteAnswerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteAnswerItemRepository extends JpaRepository<VoteAnswerItem, Long>, VoteAnswerItemRepositoryCustom {
}
