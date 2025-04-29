package com.soda.notification.controller;

import com.soda.global.response.ApiResponseForm;
import com.soda.global.response.GeneralException;
import com.soda.notification.dto.NotificationResponse;
import com.soda.notification.error.NotificationErrorCode;
import com.soda.notification.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");

        log.info("새로운 SSE 연결 요청 for User ID: {}", currentMemberId);

        try {
            SseEmitter emitter = notificationService.subscribe(currentMemberId);
            log.info("Controller: SSE 구독 요청 처리 완료 for User ID: {}", currentMemberId);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("SSE 구독 처리 중 컨트롤러에서 오류 발생 for User ID: {}", currentMemberId, e);
            throw new GeneralException(NotificationErrorCode.SSE_CONNECTION_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponseForm<Page<NotificationResponse>>> getMyNotifications(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest request) {
        Long currentMemberId = (Long) request.getAttribute("memberId");

        log.info("사용자 알림 목록 조회 요청 - User ID: {}, Pageable: {}", currentMemberId, pageable);

        Page<NotificationResponse> notificationPage = notificationService.getNotifications(currentMemberId, pageable);
        log.info("사용자 알림 목록 조회 성공 - User ID: {}, 조회된 항목 수: {}", currentMemberId, notificationPage.getNumberOfElements());
        return ResponseEntity.ok(ApiResponseForm.success(notificationPage, "알림 목록 조회 성공"));
    }
}