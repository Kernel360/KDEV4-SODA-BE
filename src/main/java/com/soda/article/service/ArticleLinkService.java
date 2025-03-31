package com.soda.article.service;

import com.soda.article.entity.ArticleLink;
import com.soda.article.repository.ArticleLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArticleLinkService {

    private final ArticleLinkRepository articleLinkRepository;

    public void save(ArticleLink link) {
        articleLinkRepository.save(link);
    }

    public List<ArticleLink> findByArticleId(Long articleId) {
        return articleLinkRepository.findByArticleId(articleId);
    }

    public ArticleLink findByArticleIdAndUrlAddressAndIsDeletedTrue(Long id, String urlAddress) {
        return articleLinkRepository.findByArticleIdAndUrlAddressAndIsDeletedTrue(id, urlAddress)
                .orElse(null);
    }
}
