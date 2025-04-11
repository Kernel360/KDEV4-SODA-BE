package com.soda.article.domain.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.enums.ArticleStatus;
import com.soda.article.enums.PriorityType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleDTO {
    private Long id;

    @NotNull(message = "Title is required")
    private String title;

    @NotNull(message = "Content is required")
    private String content;

    @NotNull(message = "Stage Id is required")
    private Long stageId;

    private PriorityType priority;
    private LocalDateTime deadLine;
    private ArticleStatus status; // article 생성 시 default PENDING

    private Long parentArticleId;
    private List<FileDTO> fileList;
    private List<LinkDTO> linkList;
    private List<CommentDTO> commentList;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileDTO {
        private String name;
        private String url;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinkDTO {
        private String urlAddress;
        private String urlDescription;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommentDTO {
        private Long id;
        private String content;
        private Long memberId;
        private LocalDateTime createdDate;
    }

}
