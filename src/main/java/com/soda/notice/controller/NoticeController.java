package com.soda.notice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notices")
@Slf4j
public class NoticeController {
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    /**
     * 클라이언트가 실시간 알림을 구독하는 엔드포인트
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 은 SSE 통신 규약
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        log.info("새로운 SSE 연결 요청");

        // 1. SseEmitter 객체 생성 및 타임아웃 설정
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        log.info("SseEmitter 생성됨. 타임아웃: {}ms", DEFAULT_TIMEOUT);

        // 2. 초기 연결 시 더미 데이터 전송 (클라이언트에서 연결 확인용)
        //    연결이 성공적으로 수립되었음을 클라이언트에게 알리기 위해 초기 이벤트를 보낼 수 있습니다.
        try {
            emitter.send(SseEmitter.event()
                            .id("0")
                            .name("connect")
                            .data("SSE connection established successfully.")
                     .comment("연결 성공") // 주석 (클라이언트에게는 안보임)
            );
            log.info("초기 연결 확인 이벤트 전송 완료");
        } catch (Exception e) {
            log.error("초기 연결 확인 이벤트 전송 중 오류 발생", e);
            emitter.completeWithError(e);
        }

        // 3.  생성된 Emitter를 반환 (Spring이 클라이언트와의 연결을 관리)
        //    아직은 생성만 하고 실제 알림 전송 로직은 다음 이슈에서 구현합니다.
        return emitter;
    }
}
