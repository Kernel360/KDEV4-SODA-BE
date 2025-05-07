package com.soda.project.domain.stage.article;

import com.soda.project.domain.stage.common.link.LinkBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ArticleLink extends LinkBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Builder
    public ArticleLink(String urlAddress, String urlDescription, Article article) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.article = article;
    }

    public static ArticleLink create(String urlAddress, String urlDescription, Article article) {
        return ArticleLink.builder()
                .urlAddress(urlAddress)
                .urlDescription(urlDescription)
                .article(article)
                .build();
    }

    public void delete() {
        this.markAsDeleted();
    }

    @Override
    public Long getDomainId() {
        return article.getId();
    }

    public void reActive() {
        this.markAsActive();
    }

    public void updateArticleLink(String urlAddress, String urlDescription) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
    }

    public void updateResponse(Article article) {
        this.article = article;
    }
}
