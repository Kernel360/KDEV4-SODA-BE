package com.soda.article.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentCreateResponse {

    private Long commentId;
    private String content;
    private String memberName;
    private Long parentCommentId;       // 대댓글이면 있어야 하고, 그냥 댓글인 경우 필요 없음

}
