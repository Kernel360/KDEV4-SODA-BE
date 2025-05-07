package com.soda.notification.entity;

import com.soda.common.BaseEntity;
import com.soda.member.domain.Member;
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


    @Builder
    public MemberNotification(Member member, Notification notification) {
        this.member = member;
        this.notification = notification;
    }

    public void Deleted() {
        this.markAsDeleted();
    }

}
