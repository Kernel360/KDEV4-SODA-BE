package com.soda.notification.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.soda.notification.domain.MemberNotification;
import com.soda.notification.domain.Notification;
import com.soda.notification.domain.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final NotificationType type;
    private final String message;
    private final String link;

    private final Long memberNoticeId;
    private final Long notificationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    private NotificationResponse(Long memberNoticeId, Long notificationId, NotificationType type,
                                 String message, String link, LocalDateTime createdAt) {
        this.memberNoticeId = memberNoticeId;
        this.notificationId = notificationId;
        this.type = type;
        this.message = message;
        this.link = link;
        this.createdAt = createdAt;
    }

    /**
     * MemberNotice 엔티티로부터 DTO를 생성하는 정적 팩토리 메서드
     *
     * @param memberNotification 조회된 MemberNotice 엔티티
     * @return 생성된 NotificationResponseDto
     */
    public static NotificationResponse fromEntity(MemberNotification memberNotification) {
        Notification notification = memberNotification.getNotification();
        return NotificationResponse.builder()
                .memberNoticeId(memberNotification.getId())
                .notificationId(notification.getId())
                .type(notification.getNotificationType())
                .message(notification.getMessage())
                .link(notification.getLink())
                .createdAt(memberNotification.getCreatedAt())
                .build();
    }
}
