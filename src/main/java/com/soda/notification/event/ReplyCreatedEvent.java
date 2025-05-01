package com.soda.notification.event;

public record ReplyCreatedEvent(
        Object source,

        Long projectId,
        Long replyId,
        Long parentCommentId,
        Long articleId,
        Long replierId,

        String replierNickname,
        String replyContent,

        Long articleAuthorId,
        String articleTitle,

        Long parentCommentAuthorId
) {
}
