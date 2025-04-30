package com.soda.project.article.strategy;

import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.ArticleLink;
import com.soda.project.article.error.ArticleErrorCode;
import com.soda.project.article.repository.ArticleLinkRepository;
import com.soda.project.article.repository.ArticleRepository;
import com.soda.common.link.dto.LinkUploadRequest;
import com.soda.common.link.strategy.LinkStrategy;
import com.soda.global.response.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleLinkStrategy implements LinkStrategy<Article, ArticleLink> {

    private final ArticleRepository articleRepository;
    private final ArticleLinkRepository articleLinkRepository;

    @Override
    public String getSupportedDomain() {
        return "article";
    }

    @Override
    public Article getDomainOrThrow(Long domainId) {
        return articleRepository.findById(domainId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.INVALID_ARTICLE));
    }

    @Override
    public void validateWriter(Long memberId, Article article) {
        if (!article.getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.INVALID_INPUT);
        }
    }

    @Override
    public ArticleLink toEntity(LinkUploadRequest.LinkUploadDTO dto, Article article) {
        return ArticleLink.builder()
                .urlAddress(dto.getUrlAddress())
                .urlDescription(dto.getUrlDescription())
                .article(article)
                .build();
    }

    @Override
    public List<ArticleLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Article article) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }

        return dtos.stream()
                .map(dto -> ArticleLink.builder()
                        .urlAddress(dto.getUrlAddress())
                        .urlDescription(dto.getUrlDescription())
                        .article(article)
                        .build())
                .toList();
    }

    @Override
    public void saveAll(List<ArticleLink> entities) {
        articleLinkRepository.saveAll(entities);
    }

    @Override
    public ArticleLink getLinkOrThrow(Long linkId) {
        return articleLinkRepository.findById(linkId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, ArticleLink link) {
        if (!link.getArticle().getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.USER_NOT_UPLOAD_ARTICLE_LINK);
        }
    }
}
