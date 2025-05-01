package com.soda.project.interfaces.dto.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.project.domain.stage.article.Article;
import com.soda.project.domain.stage.article.enums.ArticleStatus;
import com.soda.project.domain.stage.article.enums.PriorityType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleListViewResponse {

    private Long id;
    private String title;
    private String userName;
    private ArticleStatus status;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private LocalDateTime createdAt;
    private Long parentArticleId;
    private List<ArticleListViewResponse> children;     // 자식 게시글 리스트
    private boolean isDeleted;

    public static ArticleListViewResponse fromEntity(Article article) {
        List<ArticleListViewResponse> childArticleDTOs = article.getChildArticles() != null ?
                article.getChildArticles().stream()
                        .map(ArticleListViewResponse::fromEntity)
                        .toList() :
                null;

        return ArticleListViewResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .userName(article.getMember().getName())
                .status(article.getStatus())
                .priority(article.getPriority())
                .deadLine(article.getDeadline())
                .createdAt(article.getCreatedAt())
                .parentArticleId(article.getParentArticle() != null ? article.getParentArticle().getId() : null)
                .children(childArticleDTOs)
                .isDeleted(article.getIsDeleted())
                .build();
    }

    // 자식 글을 추가하는 메서드
    public ArticleListViewResponse withChildArticles(List<ArticleListViewResponse> childArticles) {
        return ArticleListViewResponse.builder()
                .id(this.getId())
                .title(this.getTitle())
                .userName(this.getUserName())
                .status(this.getStatus())
                .priority(this.getPriority())
                .deadLine(this.getDeadLine())
                .createdAt(this.getCreatedAt())
                .children(childArticles)
                .isDeleted(this.isDeleted())
                .build();
    }
}
