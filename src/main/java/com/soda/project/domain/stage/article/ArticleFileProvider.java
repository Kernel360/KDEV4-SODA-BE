package com.soda.project.domain.stage.article;

import java.util.List;
import java.util.Optional;

public interface ArticleFileProvider {
    void saveAll(List<ArticleFile> entities);
    Optional<ArticleFile> findById(Long fileId);
}
