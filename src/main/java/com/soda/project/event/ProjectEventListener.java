package com.soda.project.event;

import com.soda.project.stats.service.ProjectStatsUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectEventListener {

    private final ProjectStatsUpdateService statsUpdateService;

    @EventListener
    @Async
    public void handleProjectCreatedEvent(ProjectCreatedEvent event) {
        log.info("ProjectCreatedEvent 수신: Date = {}", event.getCreationDate());
        try {
            statsUpdateService.incrementProjectCount(event.getCreationDate());
            log.info("비동기 통계 업데이트 로직 호출 완료: DATE = {}", event.getCreationDate());
        } catch (Exception e) {
            log.error("비동기 통계 업데이트 처리 중 최종 오류 발생: Date = {}, Error={}", event.getCreationDate(), e.getMessage(), e);
        }
    }
}
