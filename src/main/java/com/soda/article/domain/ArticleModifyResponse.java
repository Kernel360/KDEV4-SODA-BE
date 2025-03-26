package com.soda.article.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.enums.PriorityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleModifyResponse {

    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private String memberName;
    private Long stageId;
    private List<ArticleFileDTO> fileList;
    private List<ArticleLinkDTO> linkList;
}
