package com.soda.project.infrastructure.stage.article;

import com.soda.project.domain.stage.article.ArticleLink;
import com.soda.project.domain.stage.article.ArticleLinkProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ArticleLinkProviderImpl implements ArticleLinkProvider {

    private final ArticleLinkRepository articleLinkRepository;

    @Override
    public void saveAll(List<ArticleLink> entities) {
        articleLinkRepository.saveAll(entities);
    }

    @Override
    public Optional<ArticleLink> findById(Long linkId) {
        return articleLinkRepository.findById(linkId);
    }
}
