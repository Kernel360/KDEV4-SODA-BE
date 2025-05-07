package com.soda.project.interfaces.stage.article.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ArticleLinkDTO {
    private Long id;
    private String urlAddress;
    private String urlDescription;
    private boolean isDeleted;
}
