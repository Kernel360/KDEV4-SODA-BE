package com.soda.project.article.repository;

import com.soda.project.domain.stage.article.VoteAnswerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteAnswerItemRepository extends JpaRepository<VoteAnswerItem, Long>, VoteAnswerItemRepositoryCustom {
}
