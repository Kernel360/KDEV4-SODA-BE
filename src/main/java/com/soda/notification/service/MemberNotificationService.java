package com.soda.notification.service;

import com.soda.notification.entity.MemberNotification;
import com.soda.notification.repository.MemberNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberNotificationService {
    private final MemberNotificationRepository memberNotificationRepository;

    public void save(MemberNotification memberNotification) {
        memberNotificationRepository.save(memberNotification);
    }

    public Page<MemberNotification> findByMemberIdAndIsDeletedFalse(Long userId, Pageable pageable) {
        return memberNotificationRepository.findByMemberIdAndIsDeletedFalse(userId, pageable);
    }
}
