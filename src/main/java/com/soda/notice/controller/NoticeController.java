package com.soda.notice.controller;

import com.soda.global.security.auth.UserDetailsImpl;
import com.soda.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;


@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 클라이언트가 실시간 알림을 구독하는 엔드포인트
     * @param userDetails 인증된 사용자 정보 (@AuthenticationPrincipal 사용)
     * @return SseEmitter 객체 또는 에러 응답
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId;
        try {
            if (userDetails == null || userDetails.getMember() == null) {
                log.warn("SSE 구독 요청 - 인증 정보 또는 사용자 정보 없음");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            userId = userDetails.getId();
            if (userId == null) {
                log.warn("SSE 구독 요청 - User ID가 null입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            log.info("새로운 SSE 연결 요청 for User ID: {}", userId);
        } catch (Exception e) {
            log.error("SSE 구독 요청 - 사용자 ID 추출 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            SseEmitter emitter = noticeService.subscribe(userId);
            log.info("Controller: SSE 구독 요청 처리 완료 for User ID: {}", userId);
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            log.error("SSE 구독 처리 중 컨트롤러에서 오류 발생 for User ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- 테스트용 임시 알림 발송 엔드포인트 ---
    @PostMapping("/send-test/{userId}")
    public ResponseEntity<Void> sendTestNotification(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {

        String message = payload.get("message");
        if (message == null) {
            log.warn("테스트 알림 전송 요청 - 'message' 내용 없음. User ID: {}", userId);
            return ResponseEntity.badRequest().build();
        }

        log.info("테스트 알림 전송 요청 수신 - User ID: {}, Message: {}", userId, message);

        try {
            noticeService.sendNotification(userId, "test_notification", payload);
            log.info("테스트 알림 전송 요청 완료 - User ID: {}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("테스트 알림 전송 중 오류 발생 - User ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}