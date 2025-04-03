package com.soda.article.domain.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.entity.Article;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.enums.PriorityType;
import com.soda.member.entity.Member;
import com.soda.project.entity.Stage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleCreateRequest {

    private Long projectId;
    private String title;
    private String content;
    private PriorityType priority;
    private LocalDateTime deadLine;
    private Long memberId;
    private Long stageId;
    private Long parentArticleId;           // 답글인 경우 필요함
    private List<ArticleLinkDTO> linkList;

    public Article toEntity(Member member, Stage stage, Article parentArticle) {
        return Article.builder()
                .title(this.title)
                .content(this.content)
                .priority(this.priority)
                .deadline(this.deadLine)
                .member(member)
                .stage(stage)
                .status(ArticleStatus.PENDING) // 기본 상태는 PENDING
                .parentArticle(parentArticle) // 답글이 있으면 부모 게시글을 설정
                .build();
    }

}
