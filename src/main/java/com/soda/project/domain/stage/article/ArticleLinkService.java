package com.soda.project.domain.stage.article;

import com.soda.project.domain.stage.article.dto.ArticleLinkDTO;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.infrastructure.stage.article.ArticleLinkRepository;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public void validateLinkSize(List<LinkUploadRequest.LinkUploadDTO> linkList) {
        if (linkList != null && linkList.size() > MAX_SIZE) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }
}
