package com.soda.project.infrastructure;

import com.soda.project.domain.stage.article.vote.VoteItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteItemRepository extends JpaRepository<VoteItem, Long> {
}
