package com.soda.notification.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import com.soda.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 1000)
    private String link;


    private Long articleId;
    private Long commentId;
    private Long replyId;
    private Long taskId;
    private Long approvalId;

    @Builder
    public Notification(NotificationType notificationType, String message, String link,
                        Long articleId, Long commentId, Long replyId, Long taskId, Long approvalId) {
        this.notificationType = notificationType;
        this.message = message;
        this.link = link;
        this.articleId = articleId;
        this.commentId = commentId;
        this.replyId = replyId;
        this.taskId = taskId;
        this.approvalId = approvalId;
    }

}