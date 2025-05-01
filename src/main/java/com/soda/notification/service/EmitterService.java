package com.soda.notification.service;

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
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간


    public SseEmitter createAndAddEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        log.info("사용자 ID '{}'에 대한 SseEmitter 생성 시작. 타임아웃: {}ms", userId, DEFAULT_TIMEOUT);

        this.emitters.put(userId, emitter);
        log.info("사용자 ID '{}'의 Emitter 추가 완료. 현재 Emitter 수: {}", userId, emitters.size());

        emitter.onCompletion(() -> {
            log.info("SSE 연결 완료됨 (onCompletion). 사용자 ID: {}", userId);
            removeEmitterInternal(userId, emitter, "완료");
        });
        emitter.onTimeout(() -> {
            log.warn("SSE 연결 시간 초과 (onTimeout). 사용자 ID: {}", userId);
        });
        emitter.onError(throwable -> {
            log.error("SSE 연결 오류 발생 (onError). 사용자 ID: {}. 오류: {}", userId, throwable.getMessage());
            removeEmitterInternal(userId, emitter, "오류");
        });

        sendConnectionEstablishedEvent(userId, emitter);

        return emitter;
    }

    /**
     * 내부 Emitter 제거 로직
     */
    private void removeEmitterInternal(Long userId, SseEmitter emitterToRemove, String reason) {
        SseEmitter currentEmitter = this.emitters.get(userId);
        if (currentEmitter != null && currentEmitter == emitterToRemove) {
            this.emitters.remove(userId);
            log.info("사용자 ID '{}'의 Emitter 제거 완료. 이유: {}. 현재 Emitter 수: {}", userId, reason, emitters.size());
        } else {
            log.warn("사용자 ID '{}'의 Emitter 제거 실패. 이유: {}. (현재 Emitter 인스턴스가 다르거나 이미 제거됨)", userId, reason);
        }
    }

    private void sendConnectionEstablishedEvent(Long userId, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .id(userId + "_connect_" + System.currentTimeMillis())
                    .name("connect")
                    .data("SSE 연결이 성공적으로 수립되었습니다. 사용자 ID: " + userId)
            );
            log.info("사용자 ID '{}'에게 초기 연결 이벤트 전송 완료.", userId);
        } catch (IOException e) {
            log.error("사용자 ID '{}'에게 초기 연결 이벤트 전송 실패. Emitter 제거 시도.", userId, e);
            removeEmitterInternal(userId, emitter, "초기 전송 실패");
        }
    }

    @Async
    public void sendNotification(Long userId, String eventName, Object data) {
        Optional<SseEmitter> emitterOptional = this.getEmitter(userId);
        if (emitterOptional.isPresent()) {
            SseEmitter emitter = emitterOptional.get();
            String currentThreadName = Thread.currentThread().getName();
            try {
                log.info("[{}] 사용자 ID '{}'에게 '{}' 이벤트 전송 시도.", currentThreadName, userId, eventName);
                emitter.send(SseEmitter.event()
                        .id(userId + "_" + eventName + "_" + System.currentTimeMillis())
                        .name(eventName)
                        .data(data)
                );
                log.info("[{}] 사용자 ID '{}'에게 '{}' 이벤트 전송 성공.", currentThreadName, userId, eventName);
            } catch (IOException e) {
                log.error("[{}] 사용자 ID '{}'에게 '{}' 이벤트 전송 실패 (IOException). Emitter 제거 시도.", currentThreadName, userId, eventName, e);
                removeEmitterInternal(userId, emitter, "전송 실패(IO)");
            } catch (IllegalStateException e) {
                log.error("[{}] 사용자 ID '{}'에게 '{}' 이벤트 전송 실패 (IllegalStateException - Emitter 완료됨). Emitter 제거 시도.", currentThreadName, userId, eventName, e);
                removeEmitterInternal(userId, emitter, "전송 실패(완료됨)");
            }
        } else {
            log.warn("[{}] 사용자 ID '{}'에 대한 활성 Emitter 없음. '{}' 이벤트 전송 건너뜀.", Thread.currentThread().getName(), userId, eventName);
        }
    }

    public Optional<SseEmitter> getEmitter(Long userId) {
        return Optional.ofNullable(this.emitters.get(userId));
    }

    public Map<Long, SseEmitter> getAllEmitters() {
        return Collections.unmodifiableMap(this.emitters);
    }
}