package com.soda.article.domain.comment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentUpdateResponse {

    private Long commentId;
    private String content;

}
