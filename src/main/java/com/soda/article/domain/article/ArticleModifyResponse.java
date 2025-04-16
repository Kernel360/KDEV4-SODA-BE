package com.soda.article.domain.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.entity.Article;
import com.soda.article.enums.PriorityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
