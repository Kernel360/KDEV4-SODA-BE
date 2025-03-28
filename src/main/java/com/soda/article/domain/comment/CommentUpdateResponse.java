package com.soda.article.domain.comment;

import com.soda.article.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateResponse {

    private Long commentId;
    private String content;

    public static CommentUpdateResponse fromEntity(Comment comment) {
        return CommentUpdateResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .build();
    }

}
