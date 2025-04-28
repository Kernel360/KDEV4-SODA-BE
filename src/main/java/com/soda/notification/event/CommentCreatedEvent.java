package com.soda.notification.event;


public record CommentCreatedEvent(
        Object source,

        Long projectId,
        Long commentId,
        Long articleId,
        Long commenterId,

        String commenterNickname,
        String commentContent,
        String articleTitle,
        Long articleAuthorId

) {
}
