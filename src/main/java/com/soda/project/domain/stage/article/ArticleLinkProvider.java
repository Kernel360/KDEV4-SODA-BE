package com.soda.project.domain.stage.article;

import java.util.List;
import java.util.Optional;

public interface ArticleLinkProvider {
    void saveAll(List<ArticleLink> entities);
    Optional<ArticleLink> findById(Long linkId);
}
