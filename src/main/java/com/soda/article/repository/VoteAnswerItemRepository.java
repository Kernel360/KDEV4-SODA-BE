package com.soda.article.repository;

import com.soda.article.entity.VoteAnswerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteAnswerItemRepository extends JpaRepository<VoteAnswerItem, Long>, VoteAnswerItemRepositoryCustom {
}
