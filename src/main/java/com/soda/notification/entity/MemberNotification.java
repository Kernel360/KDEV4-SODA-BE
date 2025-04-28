package com.soda.notification.entity;

import com.soda.common.BaseEntity;
import com.soda.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(nullable = false)
    private boolean isRead = false;

    @Builder
    public MemberNotification(Member member, Notification notification) {
        this.member = member;
        this.notification = notification;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
