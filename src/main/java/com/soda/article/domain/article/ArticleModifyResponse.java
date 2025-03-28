package com.soda.article.domain.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.enums.PriorityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleModifyResponse {

    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private String memberName;
    private String stageName;
    private List<ArticleFileDTO> fileList;
    private List<ArticleLinkDTO> linkList;
}
