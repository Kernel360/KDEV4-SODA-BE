package com.soda.notification.service;

import com.soda.notification.dto.NotificationResponse;
import com.soda.notification.entity.MemberNotification;
import com.soda.notification.entity.Notification;
import com.soda.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmitterService emitterService;
    private final NotificationRepository notificationRepository;
    private final MemberNotificationService memberNotificationService;

    /**
     * 사용자의 알림 구독 요청을 처리합니다.
     * EmitterService를 통해 SSE 연결을 생성하고 관리합니다.
     *
     * @param userId 구독을 요청하는 사용자의 ID
     * @return 생성된 SseEmitter 객체
     * @throws RuntimeException Emitter 생성/처리 중 오류 발생 시
     */
    public SseEmitter subscribe(Long userId) {
        log.info("알림 구독 서비스 시작 for User ID: {}", userId);
        try {
            SseEmitter emitter = emitterService.createAndAddEmitter(userId);
            log.info("Emitter 생성 및 등록 완료 by EmitterService for User ID: {}", userId);
            return emitter;
        } catch (Exception e) {
            log.error("알림 구독 처리 중 오류 발생 for User ID: {}", userId, e);
            throw new RuntimeException("SSE 구독 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 특정 사용자에게 알림 데이터를 전송합니다.
     * 실제 데이터 전송은 EmitterService에 위임합니다.
     * @param userId 알림을 받을 사용자의 ID
     * @param eventName 이벤트 이름 (예: "new_notice", "task_update")
     * @param noticeData 전송할 알림 데이터 (DTO, String 등)
     */
    public void sendNotification(Long userId, String eventName, Object noticeData) {
        log.info("알림 전송 요청 to User ID: {}, Event: {}", userId, eventName);
        emitterService.sendNotification(userId, eventName, noticeData);
    }

    public Notification save(Notification notification) {
        notificationRepository.save(notification);
        return notification;
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        log.info("사용자 알림 목록 조회 서비스 시작 - User ID: {}, Pageable: {}", userId, pageable);

        Page<MemberNotification> memberNoticePage = memberNotificationService.findByMemberIdAndIsDeletedFalse(userId, pageable);

        Page<NotificationResponse> responseDtoPage = memberNoticePage.map(NotificationResponse::fromEntity);

        log.info("사용자 알림 목록 조회 완료 - User ID: {}, Found: {} items", userId, responseDtoPage.getTotalElements());
        return responseDtoPage;
    }

    /**
     * 사용자의 특정 알림을 읽음 상태로 변경합니다.
     *
     * @param userId              요청한 사용자의 ID
     * @param memberNotificationId 읽음 처리할 MemberNotification의 ID
     */
    @Transactional
    public void markAsRead(Long userId, Long memberNotificationId) {
        log.debug("알림 읽음 처리 서비스 시작 - User ID: {}, MemberNotification ID: {}", userId, memberNotificationId);

        MemberNotification memberNotification = memberNotificationService.findById(memberNotificationId);

        memberNotification.Deleted();

        log.debug("알림 읽음 처리 완료 및 저장 예정 - MemberNotification ID: {}", memberNotificationId);
    }
}