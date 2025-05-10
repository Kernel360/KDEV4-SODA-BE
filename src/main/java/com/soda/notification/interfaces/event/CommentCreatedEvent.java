package com.soda.notification.interfaces.event;


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
