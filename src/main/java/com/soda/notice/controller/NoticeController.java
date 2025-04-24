package com.soda.notice.controller;

import com.soda.global.security.auth.UserDetailsImpl; // UserDetailsImpl import 확인
import com.soda.notice.service.NoticeService; // NoticeService 사용
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// import java.io.IOException; // 더 이상 필요 없음

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    // EmitterService 대신 NoticeService 주입
    private final NoticeService noticeService;
    // DEFAULT_TIMEOUT 도 Controller 에서는 더 이상 필요 없음

    /**
     * 클라이언트가 실시간 알림을 구독하는 엔드포인트
     * @param userDetails 인증된 사용자 정보 (@AuthenticationPrincipal 사용)
     * @return SseEmitter 객체 또는 에러 응답
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId;
        try {
            // 사용자 ID 추출 및 유효성 검증
            if (userDetails == null || userDetails.getMember() == null) {
                log.warn("SSE 구독 요청 - 인증 정보 또는 사용자 정보 없음");
                // 인증 안됐거나 사용자 정보 없으면 401 Unauthorized 반환
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // UserDetailsImpl에서 사용자 ID 가져오기 (getMember().getId() 또는 직접 getId())
            userId = userDetails.getId(); // UserDetailsImpl에 getId()가 있다고 가정
            if (userId == null) {
                log.warn("SSE 구독 요청 - User ID가 null입니다.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // ID 없으면 400 Bad Request
            }
            log.info("새로운 SSE 연결 요청 for User ID: {}", userId);
        } catch (Exception e) {
            // 사용자 정보 추출 중 예외 발생 시 (예: 캐스팅 실패 등)
            log.error("SSE 구독 요청 - 사용자 ID 추출 실패", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            // NoticeService의 구독 메서드 호출
            SseEmitter emitter = noticeService.subscribe(userId);
            log.info("Controller: SSE 구독 요청 처리 완료 for User ID: {}", userId);
            // 성공 시 200 OK 와 함께 SseEmitter 반환
            return ResponseEntity.ok(emitter);
        } catch (Exception e) {
            // 서비스 레이어에서 발생한 예외 처리
            log.error("SSE 구독 처리 중 컨트롤러에서 오류 발생 for User ID: {}", userId, e);
            // 서버 내부 오류로 500 Internal Server Error 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}