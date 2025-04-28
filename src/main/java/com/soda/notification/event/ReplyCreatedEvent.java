package com.soda.notification.event;

public record ReplyCreatedEvent(
        Object source,

        Long projectId,
        Long replyId,
        Long parentCommentId,
        Long postId,
        Long replierId,

        String replierNickname,
        String replyContent,

        Long postAuthorId,
        String postTitle,

        Long parentCommentAuthorId
) {
}
