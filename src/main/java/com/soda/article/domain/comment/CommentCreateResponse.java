package com.soda.article.domain.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentCreateResponse {

    private Long commentId;
    private String content;
    private String memberName;
    private Long parentCommentId;       // 대댓글이면 있어야 하고, 그냥 댓글인 경우 필요 없음

}
