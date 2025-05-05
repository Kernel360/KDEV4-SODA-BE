package com.soda.project.interfaces.dto.stage.article.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateRequest {

    private String content;

}
