package com.soda.article.domain.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateRequest {

    private String content;

}
