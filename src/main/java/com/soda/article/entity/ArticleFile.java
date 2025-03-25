package com.soda.article.entity;

import com.soda.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ArticleFile extends BaseEntity {

    private String name;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Builder
    public ArticleFile (String name, String url, Article article) {
        this.name = name;
        this.url = url;
        this.article = article;
    }
}
