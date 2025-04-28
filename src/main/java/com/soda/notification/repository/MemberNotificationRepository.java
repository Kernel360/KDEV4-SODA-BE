package com.soda.notification.repository;

import com.soda.notification.entity.MemberNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {
}
