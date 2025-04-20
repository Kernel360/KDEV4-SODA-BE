package com.soda.article.dto.comment;

import com.soda.article.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateResponse {

    private Long id;
    private String content;

    public static CommentUpdateResponse fromEntity(Comment comment) {
        return CommentUpdateResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .build();
    }

}
