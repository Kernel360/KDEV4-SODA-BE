package com.soda.article.domain.article;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleLinkDTO {

    private String urlAddress;
    private String urlDescription;
}
