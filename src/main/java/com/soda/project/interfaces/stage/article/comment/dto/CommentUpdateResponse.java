package com.soda.project.interfaces.stage.article.comment.dto;

import com.soda.project.domain.stage.article.comment.Comment;
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
