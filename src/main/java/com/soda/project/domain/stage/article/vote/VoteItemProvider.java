package com.soda.project.domain.stage.article.vote;

import java.util.List;

public interface VoteItemProvider {
    List<VoteItem> storeAll(List<VoteItem> voteItems);

    List<VoteItem> findAllById(List<Long> itemIds);
}
