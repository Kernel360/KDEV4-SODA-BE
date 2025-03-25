package com.soda.article.entity;

import com.soda.common.BaseEntity;
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
public class ArticleLink extends BaseEntity {

    private String urlAddress;

    private String urlDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Builder
    public ArticleLink(String urlAddress, String urlDescription, Article article) {
        this.urlAddress = urlAddress;
        this.urlDescription = urlDescription;
        this.article = article;
    }

}
