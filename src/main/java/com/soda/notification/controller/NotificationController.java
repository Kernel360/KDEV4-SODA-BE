package com.soda.notification.controller;

import com.soda.global.response.GeneralException;
import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.notification.dto.NotificationResponse;
import com.soda.notification.error.NotificationErrorCode;
import com.soda.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 클라이언트가 실시간 알림을 구독하는 엔드포인트
     *
     * @param userDetails 인증된 사용자 정보 (@AuthenticationPrincipal 사용)
     * @return SseEmitter 객체 또는 에러 응답
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId;
        try {
            if (userDetails == null || userDetails.getMember() == null) {
                throw new GeneralException(NotificationErrorCode.USER_INFO_NOT_FOUND);
            }
            userId = userDetails.getId();
            if (userId == null) {
                throw new GeneralException(NotificationErrorCode.USER_ID_NULL);
            }
            log.info("새로운 SSE 연결 요청 for User ID: {}", userId);
        } catch (Exception e) {
            log.error("SSE 구독 요청 - 사용자 ID 추출 실패", e);
            throw new GeneralException(NotificationErrorCode.USER_ID_PARSE_FAILED);
        }

        try {
            SseEmitter emitter = notificationService.subscribe(userId);
            log.info("Controller: SSE 구독 요청 처리 완료 for User ID: {}", userId);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("SSE 구독 처리 중 컨트롤러에서 오류 발생 for User ID: {}", userId, e);
            throw new GeneralException(NotificationErrorCode.SSE_CONNECTION_ERROR);
        }
    }

    /**
     * 현재 로그인한 사용자의 알림 목록을 페이징하여 조회합니다.
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @param pageable    페이징 정보
     * @return 페이징된 알림 목록 응답
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = userDetails.getId();
        log.info("사용자 알림 목록 조회 요청 - User ID: {}, Pageable: {}", userId, pageable);
        try {
            Page<NotificationResponse> notificationPage = notificationService.getNotifications(userId, pageable);
            return ResponseEntity.ok(notificationPage);
        } catch (Exception e) {
            log.error("알림 목록 조회 중 오류 발생 - User ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}