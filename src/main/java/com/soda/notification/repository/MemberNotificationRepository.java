package com.soda.notification.repository;

import com.soda.notification.entity.MemberNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationRepository extends JpaRepository<MemberNotification, Long> {

    @EntityGraph(attributePaths = {"notification"})
    Page<MemberNotification> findByMemberIdAndIsDeletedFalse(Long memberId, Pageable pageable);

}
