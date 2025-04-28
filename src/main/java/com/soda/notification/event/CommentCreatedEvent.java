package com.soda.notification.event;


public record CommentCreatedEvent(
        Object source,

        Long projectId,
        Long commentId,
        Long postId,
        Long commenterId,

        String commenterNickname,
        String commentContent,
        String postTitle,
        Long postAuthorId

) {
}
