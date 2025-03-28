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
public class ArticleCreateResponse {

    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private String memberName;
    private String stageName;
    private Long parentArticleId;           // 답글인 경우를 나타내기 위해서
    private List<ArticleFileDTO> fileList;
    private List<ArticleLinkDTO> linkList;

    public static ArticleCreateResponse fromEntity(Article article) {
        return ArticleCreateResponse.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageName(article.getStage().getName())
                .parentArticleId(article.getParentArticle().getId())
                .fileList(article.getArticleFileList().stream()
                        .map(file -> ArticleFileDTO.builder()
                                .name(file.getName())
                                .url(file.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .linkList(article.getArticleLinkList().stream()
                        .map(link -> ArticleLinkDTO.builder()
                                .urlAddress(link.getUrlAddress())
                                .urlDescription(link.getUrlDescription())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}
