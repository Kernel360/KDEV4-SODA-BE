package com.soda.project.interfaces.stage.article.comment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateRequest {

    private String content;

}
