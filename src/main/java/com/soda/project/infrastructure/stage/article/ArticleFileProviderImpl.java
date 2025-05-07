package com.soda.project.infrastructure.stage.article;

import com.soda.project.domain.stage.article.ArticleFile;
import com.soda.project.domain.stage.article.ArticleFileProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleFileProviderImpl implements ArticleFileProvider {

    private final ArticleFileRepository articleFileRepository;

    @Override
    public void saveAll(List<ArticleFile> entities) {
        articleFileRepository.saveAll(entities);
    }

    @Override
    public Optional<ArticleFile> findById(Long fileId) {
        return articleFileRepository.findById(fileId);
    }
}
