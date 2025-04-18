package com.soda.article.domain.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentCreateRequest {

    private Long projectId;
    private Long articleId;
    private String content;
    private Long parentCommentId;

}
