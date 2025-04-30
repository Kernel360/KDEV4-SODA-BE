package com.soda.project.article.dto.article;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleFileDTO {
    private Long id;
    private String name;
    private String url;
    private boolean isDeleted;
}
