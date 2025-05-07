package com.soda.project.domain.stage.article;

import com.soda.global.response.GeneralException;
import com.soda.project.domain.stage.article.error.ArticleErrorCode;
import com.soda.project.domain.stage.common.link.LinkStrategy;
import com.soda.project.interfaces.stage.common.link.dto.LinkUploadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleLinkStrategy implements LinkStrategy<Article, ArticleLink> {

    private final ArticleProvider articleProvider;
    private final ArticleLinkProvider articleLinkProvider;

    @Override
    public String getSupportedDomain() {
        return "article";
    }

    @Override
    public Article getDomainOrThrow(Long domainId) {
        return articleProvider.findById(domainId)
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
        return ArticleLink.create(dto.getUrlAddress(), dto.getUrlDescription(), article);
    }

    @Override
    public List<ArticleLink> toEntities(List<LinkUploadRequest.LinkUploadDTO> dtos, Article article) {
        if (dtos == null || dtos.isEmpty()) {return List.of();}

        return dtos.stream()
                .map(dto -> ArticleLink.create(dto.getUrlAddress(), dto.getUrlDescription(), article))
                .toList();
    }

    @Override
    public void saveAll(List<ArticleLink> entities) {
        articleLinkProvider.saveAll(entities);
    }

    @Override
    public ArticleLink getLinkOrThrow(Long linkId) {
        return articleLinkProvider.findById(linkId)
                .orElseThrow(() -> new GeneralException(ArticleErrorCode.ARTICLE_LINK_NOT_FOUND));
    }

    @Override
    public void validateLinkUploader(Long memberId, ArticleLink link) {
        if (!link.getArticle().getMember().getId().equals(memberId)) {
            throw new GeneralException(ArticleErrorCode.USER_NOT_UPLOAD_ARTICLE_LINK);
        }
    }
}
