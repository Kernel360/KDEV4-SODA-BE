package com.soda.notification.listener;

import com.soda.member.entity.Member;
import com.soda.notification.dto.NotificationData;
import com.soda.notification.entity.MemberNotification;
import com.soda.notification.entity.Notification;
import com.soda.notification.event.CommentCreatedEvent;
import com.soda.notification.event.ReplyCreatedEvent;
import com.soda.notification.service.MemberNotificationService;
import com.soda.notification.service.NotificationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentNotificationListener {

    private final NotificationService notificationService;
    private final MemberNotificationService memberNotificationService;
    private final EntityManager entityManager;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleEnrichedCommentCreated(CommentCreatedEvent event) {
        log.info("Event Received After Commit: EnrichedCommentCreatedEvent for comment {}", event.commentId());
        try {
            Long articleAuthorId = event.articleAuthorId();
            Long commenterId = event.commenterId();

            // 본인 댓글 제외 (이벤트 발행 시점에 이미 체크했지만, 안전을 위해 한번 더 확인 가능)
            if (!Objects.equals(commenterId, articleAuthorId)) {
                // 이벤트 정보로 알림 데이터 생성
                String message = String.format("'%s'님이 '%s' 게시글에 댓글을 남겼습니다:\n%s",
                        event.commenterNickname(), event.articleTitle(), truncateContent(event.commentContent()));
                String link = String.format("/user/projects/%d/article/%d", event.articleId(), event.commentId());

                // NotificationData의 정적 팩토리 메서드 사용
                NotificationData notificationData = NotificationData.forNewComment(
                        message, link, event.projectId(), event.articleId(), event.commentId()
                );

                // 알림 전송
                notificationService.sendNotification(articleAuthorId, "new_comment", notificationData);

                // DB 알림 저장
                saveNotification(articleAuthorId, notificationData);
            }
        } catch (Exception e) {
            log.error("Error handling EnrichedCommentCreatedEvent: {}", event, e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleEnrichedReplyCreated(ReplyCreatedEvent event) {
        log.info("Event Received After Commit: EnrichedReplyCreatedEvent for reply {}", event.replyId());
        try {
            Long replierId = event.replierId();
            Long articleAuthorId = event.articleAuthorId();
            Long parentCommentAuthorId = event.parentCommentAuthorId();

            Set<Long> targetUserIds = new HashSet<>();
            // 게시글 작성자 추가 (본인 대댓글 제외)
            if (!Objects.equals(replierId, articleAuthorId)) {
                targetUserIds.add(articleAuthorId);
            }
            // 부모 댓글 작성자 추가 (본인 대댓글 제외)
            if (!Objects.equals(replierId, parentCommentAuthorId)) {
                targetUserIds.add(parentCommentAuthorId);
            }

            // 각 대상자에게 알림 전송
            for (Long targetUserId : targetUserIds) {
                NotificationData notificationData;
                String eventName;

                String link = String.format("/user/projects/%d/article/%d", event.projectId(), event.articleId(), event.replyId());
                String truncatedContent = truncateContent(event.replyContent());

                if (Objects.equals(targetUserId, articleAuthorId)) {
                    String message = String.format("'%s'님이 '%s' 게시글의 댓글에 답글을 남겼습니다:\n%s",
                            event.replierNickname(), event.articleTitle(), truncatedContent);
                    notificationData = NotificationData.forNewReplyOnPost(message, link, event.projectId(), event.articleId(), event.parentCommentId(), event.replyId());
                    eventName = "new_reply_on_article";
                } else {
                    String message = String.format("'%s'님이 회원님의 댓글에 답글을 남겼습니다:\n%s",
                            event.replierNickname(), truncatedContent);
                    notificationData = NotificationData.forNewReplyToMyComment(message, link, event.projectId(), event.articleId(), event.parentCommentId(), event.replyId());
                    eventName = "new_reply";
                }

                notificationService.sendNotification(targetUserId, eventName, notificationData);

                // DB 알림 저장
                saveNotification(articleAuthorId, notificationData);
            }
        } catch (Exception e) {
            log.error("Error handling EnrichedReplyCreatedEvent: {}", event, e);
        }
    }

    /**
     * Notification 엔티티를 생성하고 DB에 저장합니다.
     * Member 엔티티는 ID를 이용해 프록시 객체로 참조합니다.
     *
     * @param receiverId 알림 수신자 ID
     * @param data       알림 데이터 DTO
     */
    private void saveNotification(Long receiverId, NotificationData data) {
        try {
            // <<< EntityManager를 사용하여 Member 프록시 객체 가져오기 >>>
            Member receiverProxy = entityManager.getReference(Member.class, receiverId);

            Notification notification = Notification.builder()
                    .notificationType(data.type())
                    .message(data.message())
                    .link(data.link())
                    .articleId(data.articleId())
                    .commentId(data.commentId())
                    .replyId(data.replyId())
                    .taskId(data.requestId())
                    .approvalId(data.responseId())
                    .build();

            MemberNotification memberNotification = MemberNotification.builder()
                    .member(receiverProxy)
                    .notification(notification)
                    .build();

            memberNotificationService.save(memberNotification);
            notificationService.save(notification);

            log.info("알림 저장 완료 for User ID: {}", receiverId);

        } catch (jakarta.persistence.EntityNotFoundException enfe) {
            log.error("알림 저장 실패: 프록시 대상 Member 엔티티를 찾을 수 없습니다. ID: {}", receiverId, enfe);
        } catch (Exception e) {
            log.error("알림 DB 저장 실패 for User ID: {}, Data: {}", receiverId, data, e);
        }
    }

    private String truncateContent(String content) {
        int maxLength = 50;
        if (content == null) return "";
        return content.length() <= maxLength ? content : content.substring(0, maxLength) + "...";
    }
}