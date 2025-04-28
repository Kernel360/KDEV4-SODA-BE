package com.soda.notice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class EmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간 (필요시 조정)

    /**
     * 지정된 사용자 ID에 대한 SseEmitter를 생성하고 저장합니다.
     * 생성 시 완료, 타임아웃, 에러 콜백을 등록하여 자동으로 제거되도록 합니다.
     * 초기 연결 확인 이벤트도 전송합니다.
     * @param userId 사용자 ID
     * @return 생성된 SseEmitter 객체
     */
    public SseEmitter createAndAddEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        log.info("Creating SseEmitter for User ID: {}. Timeout: {}ms", userId, DEFAULT_TIMEOUT);

        this.emitters.put(userId, emitter);
        log.info("Emitter added for User ID: {}. Current emitter count: {}", userId, emitters.size());

        emitter.onCompletion(() -> {
            log.info("SSE connection completed (onCompletion) for User ID: {}", userId);
            removeEmitterInternal(userId, emitter, "completion");
        });
        emitter.onTimeout(() -> {
            log.warn("SSE connection timed out (onTimeout) for User ID: {}", userId);
        });
        emitter.onError(throwable -> {
            log.error("SSE connection error (onError) for User ID: {}. Error: {}", userId, throwable.getMessage());
            removeEmitterInternal(userId, emitter, "error");
        });

        sendConnectionEstablishedEvent(userId, emitter);

        return emitter;
    }

    private void removeEmitterInternal(Long userId, SseEmitter emitterToRemove, String reason) {
        SseEmitter currentEmitter = this.emitters.get(userId);
        if (currentEmitter != null && currentEmitter == emitterToRemove) {
            this.emitters.remove(userId);
            log.info("Emitter removed successfully for User ID: {} due to {}. Current emitter count: {}", userId, reason, emitters.size());
        } else {
            log.warn("Emitter not removed for User ID: {}. Reason: {}. (Instance mismatch or already removed)", userId, reason);
        }
    }

    private void sendConnectionEstablishedEvent(Long userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(userId + "_connect_" + System.currentTimeMillis())
                    .name("connect")
                    .data("SSE connection established successfully. UserID: " + userId)
            );
            log.info("Sent initial connection event for User ID: {}", userId);
        } catch (IOException e) {
            log.error("Failed to send initial connection event for User ID: {}. Removing emitter.", userId, e);
            removeEmitterInternal(userId, emitter, "initial_send_failure");
        }
    }

    @Async
    public void sendNotification(Long userId, String eventName, Object data) {
        Optional<SseEmitter> emitterOptional = this.getEmitter(userId);
        if (emitterOptional.isPresent()) {
            SseEmitter emitter = emitterOptional.get();
            try {
                log.info("[{}] Sending notification event '{}' to User ID: {}", Thread.currentThread().getName(), eventName, userId); 
                emitter.send(SseEmitter.event()
                        .id(userId + "_" + eventName + "_" + System.currentTimeMillis())
                        .name(eventName)
                        .data(data)
                );
                log.info("[{}] Sent notification event '{}' to User ID: {}", Thread.currentThread().getName(), eventName, userId);
            } catch (IOException e) {
                log.error("[{}] Failed to send notification event '{}' to User ID: {}. Removing emitter.", Thread.currentThread().getName(), eventName, userId, e);
                removeEmitterInternal(userId, emitter, "send_failure");
            } catch (IllegalStateException e) {
                log.error("[{}] Failed to send notification event '{}' to User ID: {} (emitter completed). Removing emitter.", Thread.currentThread().getName(), eventName, userId, e);
                removeEmitterInternal(userId, emitter, "emitter_completed");
            }
        } else {
            log.warn("[{}] No active emitter found for User ID: {} to send event '{}'. Notification skipped.", Thread.currentThread().getName(), userId, eventName);
        }
    }

    public Optional<SseEmitter> getEmitter(Long userId) {
        return Optional.ofNullable(this.emitters.get(userId));
    }

    public Map<Long, SseEmitter> getAllEmitters() {
        return Collections.unmodifiableMap(this.emitters);
    }
}