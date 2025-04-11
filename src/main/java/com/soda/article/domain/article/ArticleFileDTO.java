package com.soda.article.domain.article;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleFileDTO {
    private String name;
    private String url;
}
