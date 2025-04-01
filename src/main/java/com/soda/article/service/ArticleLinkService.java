package com.soda.article.service;

import com.soda.article.domain.article.ArticleLinkDTO;
import com.soda.article.entity.Article;
import com.soda.article.entity.ArticleFile;
import com.soda.article.entity.ArticleLink;
import com.soda.article.error.ArticleErrorCode;
import com.soda.article.repository.ArticleLinkRepository;
import com.soda.global.response.GeneralException;
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
    private static final int MAX_SIZE = 10;

    public void processLinks(List<ArticleLinkDTO> linkList, Article article) {
        if (linkList != null) {
            linkList.forEach(articleLinkDTO -> {
                ArticleLink link = processLink(articleLinkDTO, article);
                article.getArticleLinkList().add(link);
            });
        }
    }

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

    public void validateLinkSize(List<ArticleLinkDTO> linkList) {
        if (linkList != null && linkList.size() > MAX_SIZE) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }
}
