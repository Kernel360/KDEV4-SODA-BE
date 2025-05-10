package com.soda.notification.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soda.notification.domain.NotificationType;

/**
 * @param type       알림 종류
 * @param message    사용자에게 보여줄 메시지
 * @param link       클릭 시 이동할 링크
 * @param articleId  관련 게시글 ID
 * @param commentId  관련 댓글 ID
 * @param replyId    관련 대댓글 ID
 * @param requestId  관련 작업 ID
 * @param responseId 관련 승인요청 ID
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationData(NotificationType type, String message, String link, Long projectId, Long articleId,
                               Long commentId,
                               Long replyId, Long requestId, Long responseId) {

    public static NotificationData forNewComment(String message, String link, Long projectId, Long articleId, Long commentId) {
        return new NotificationData(NotificationType.NEW_COMMENT_ON_POST, message, link, projectId, articleId, commentId, null, null, null);
    }

    public static NotificationData forNewReplyOnPost(String message, String link, Long projectId, Long articleId, Long parentCommentId, Long replyId) {
        return new NotificationData(NotificationType.NEW_REPLY_ON_POST, message, link, projectId, articleId, parentCommentId, replyId, null, null);
    }

    public static NotificationData forNewReplyToMyComment(String message, String link, Long projectId, Long articleId, Long parentCommentId, Long replyId) {
        return new NotificationData(NotificationType.NEW_REPLY_TO_MY_COMMENT, message, link, projectId, articleId, parentCommentId, replyId, null, null);
    }
}
