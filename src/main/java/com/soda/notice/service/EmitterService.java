package com.soda.notice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SseEmitter 객체를 중앙에서 관리하는 서비스
 * 사용자 ID를 키로 사용하여 Emitter를 저장, 조회, 삭제합니다.
 */
@Service
@Slf4j
public class EmitterService {

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 지정된 사용자 ID에 대한 SseEmitter를 저장합니다.
     *
     * @param userId 사용자 ID
     * @param emitter 저장할 SseEmitter 객체
     */
    public void addEmitter(Long userId, SseEmitter emitter) {
        this.emitters.put(userId, emitter);
        log.info("Emitter added for user ID: {}. Current emitter count: {}", userId, emitters.size());
    }

    /**
     * 지정된 사용자 ID에 해당하는 SseEmitter를 제거합니다.
     *
     * @param userId 사용자 ID
     */
    public void removeEmitter(Long userId) {
        SseEmitter removedEmitter = this.emitters.remove(userId);
        if (removedEmitter != null) {
            log.info("Emitter removed successfully for user ID: {}. Current emitter count: {}", userId, emitters.size());
        } else {
            log.warn("Emitter not found for removal for user ID: {}", userId);
        }
    }

    /**
     * 지정된 사용자 ID에 해당하는 SseEmitter를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 SseEmitter Optional 객체 (없으면 Optional.empty())
     */
    public Optional<SseEmitter> getEmitter(Long userId) {
        return Optional.ofNullable(this.emitters.get(userId));
    }

    /**
     * (Optional) 현재 활성화된 모든 Emitter 맵의 불변 뷰를 반환합니다.
     * Heartbeat 등 전체 Emitter에 대한 작업 시 사용될 수 있습니다.
     * 주의: 반환된 맵은 수정할 수 없습니다.
     *
     * @return 사용자 ID를 키로, SseEmitter를 값으로 가지는 불변 맵
     */
    public Map<Long, SseEmitter> getAllEmitters() {
        return Collections.unmodifiableMap(this.emitters);
    }

}
