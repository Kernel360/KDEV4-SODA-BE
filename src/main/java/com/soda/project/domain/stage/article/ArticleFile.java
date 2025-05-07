package com.soda.project.domain.stage.article;

import com.soda.project.domain.stage.common.file.FileBase;
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
public class ArticleFile extends FileBase {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Builder
    public ArticleFile (String name, String url, Article article) {
        this.name = name;
        this.url = url;
        this.article = article;
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

    public void updateArticleFile(String name, String url) {
        this.name = name;
        this.url = url;
    }
}
