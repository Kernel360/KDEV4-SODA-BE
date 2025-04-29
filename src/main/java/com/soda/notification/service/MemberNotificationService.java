package com.soda.notification.service;

import com.soda.global.response.GeneralException;
import com.soda.notification.entity.MemberNotification;
import com.soda.notification.error.NotificationErrorCode;
import com.soda.notification.repository.MemberNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public MemberNotification findByIdOrThrow(Long id) {
        return memberNotificationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("MemberNotificationService: 알림을 찾을 수 없음 - ID: {}", id);
                    return new GeneralException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
                });
    }

    public List<MemberNotification> findByMemberIdAndIsReadFalse(Long userId) {
        return memberNotificationRepository.findByMemberIdAndIsDeletedFalse(userId);
    }
}
