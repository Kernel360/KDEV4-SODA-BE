package com.soda.article.domain.comment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.article.entity.Comment;
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

    public static CommentCreateResponse fromEntity(Comment comment) {
        Long parentCommentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;

        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .memberName(comment.getMember().getName())
                .parentCommentId(parentCommentId)
                .build();
    }

}
