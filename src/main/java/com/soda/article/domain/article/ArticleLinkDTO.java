package com.soda.article.domain.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleLinkDTO {

    private String urlAddress;
    private String urlDescription;
}
