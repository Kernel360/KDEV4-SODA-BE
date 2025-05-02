package com.soda.project.domain.stage.article.dto;

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
