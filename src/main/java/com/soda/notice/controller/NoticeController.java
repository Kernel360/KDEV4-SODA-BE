package com.soda.notice.controller;

import com.soda.global.security.auth.UserDetailsImpl; // UserDetailsImpl import 확인
import com.soda.notice.service.EmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final EmitterService emitterService;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    /**
     * 클라이언트가 실시간 알림을 구독하는 엔드포인트
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 은 SSE 통신 규약
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (userDetails == null || userDetails.getMember() == null) {
            log.warn("SSE 구독 요청 - 인증 정보 또는 사용자 정보 없음");
            throw new IllegalArgumentException("사용자 인증 정보를 찾을 수 없습니다.");
        }
        Long userId = userDetails.getId();
        log.info("새로운 SSE 연결 요청 for User ID: {}", userId);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        log.info("SseEmitter 생성됨 for User ID: {}. 타임아웃: {}ms", userId, DEFAULT_TIMEOUT);
        emitterService.addEmitter(userId, emitter);
        log.info("Emitter 저장됨 for User ID: {}", userId);

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료 (onCompletion) for User ID: {}", userId);
            emitterService.removeEmitter(userId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 연결 시간 초과 (onTimeout) for User ID: {}", userId);
            emitter.complete();
        });

        emitter.onError(throwable -> {
            log.error("SSE 연결 오류 발생 (onError) for User ID: {}. Error: {}", userId, throwable.getMessage());
            emitterService.removeEmitter(userId);
        });

        try {
            emitter.send(SseEmitter.event()
                    .id(userId + "_" + System.currentTimeMillis())
                    .name("connect")
                    .data("SSE connection established successfully. UserID: " + userId)
            );
            log.info("초기 연결 확인 이벤트 전송 완료. 사용자 ID: {}", userId);
        } catch (IOException e) {
            log.error("초기 연결 확인 이벤트 전송 중 오류 발생. 사용자 ID: {}", userId, e);
            emitterService.removeEmitter(userId);
        }

        return emitter;
    }
}