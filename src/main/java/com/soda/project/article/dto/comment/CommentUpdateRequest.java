package com.soda.project.article.dto.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateRequest {

    private String content;

}
