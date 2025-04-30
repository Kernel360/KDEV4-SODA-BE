package com.soda.project.article.dto.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.article.enums.PriorityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleModifyResponse {

    private Long id;
    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private String memberName;
    private String stageName;

    public static ArticleModifyResponse fromEntity(Article article) {
        return ArticleModifyResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageName(article.getStage().getName())
                .build();
    }
}
