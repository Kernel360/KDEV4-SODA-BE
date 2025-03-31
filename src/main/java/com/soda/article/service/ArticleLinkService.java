package com.soda.article.service;

import com.soda.article.domain.article.ArticleLinkDTO;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
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

    public ArticleLink processLink(ArticleLinkDTO linkDTO, Article article) {
        ArticleLink link = articleLinkRepository.findByArticleIdAndUrlAddressAndIsDeletedTrue(article.getId(), linkDTO.getUrlAddress())
                .orElse(null);

        if (link != null) {
            link.reActive();
        } else {
            link = ArticleLink.builder()
                    .urlAddress(linkDTO.getUrlAddress())
                    .urlDescription(linkDTO.getUrlDescription())
                    .article(article)
                    .build();
        }

        articleLinkRepository.save(link);
        return link;
    }

    public void deleteLinks(Long articleId, Article article) {
        List<ArticleLink> existingLinks = articleLinkRepository.findByArticleId(articleId);
        existingLinks.forEach(ArticleLink::delete);
        article.getArticleLinkList().removeIf(existingLinks::contains);
    }
}
