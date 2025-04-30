package com.soda.project.interfaces.dto.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleViewResponse {
    private String title;
    private String content;
    private ArticleStatus status;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private String memberName;
    private String stageName;
    private List<ArticleFileDTO> fileList;
    private List<ArticleLinkDTO> linkList;
    private LocalDateTime createdAt;

    public static ArticleViewResponse fromEntity(Article article) {
        return ArticleViewResponse.builder()
                .title(article.getTitle())
                .content(article.getContent())
                .status(article.getStatus())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .memberName(article.getMember().getName())
                .stageName(article.getStage().getName())
                .createdAt(article.getCreatedAt())
                .fileList(article.getArticleFileList().stream()
                        .map(file -> ArticleFileDTO.builder()
                                .id(file.getId())
                                .name(file.getName())
                                .url(file.getUrl())
                                .isDeleted(file.getIsDeleted())
                                .build())
                        .collect(Collectors.toList()))
                .linkList(article.getArticleLinkList().stream()
                        .map(link -> ArticleLinkDTO.builder()
                                .id(link.getId())
                                .urlAddress(link.getUrlAddress())
                                .urlDescription(link.getUrlDescription())
                                .isDeleted(link.getIsDeleted())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
