package com.soda.project.application.stats;

import com.soda.project.domain.stats.ProjectDailyStats;
import com.soda.project.infrastructure.ProjectDailyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatsUpdateService {
    private final ProjectDailyStatsRepository statsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementProjectCount(LocalDate date) {
        log.info("통계 업데이트 시작: Date = {}", date);

        try {
            // 해당 날짜의 통계 조회 (없으면 새로 생성)
            ProjectDailyStats stats = statsRepository.findByStatDate(date)
                    .orElseGet(() -> {
                        log.info("해당 날짜({})의 통계 없음. 새로 생성 시작", date);
                        return ProjectDailyStats.builder()
                                .statDate(date)
                                .creationCount(0L) // 초기값 0
                                .build();
                    });

            // count 증가
            stats.incrementCount();

            // DB 저장
            statsRepository.save(stats);
            log.info("통계 업데이트 완료 : Date={}, New Count={}", date, stats.getCreationCount());
        } catch (Exception e) {
            // 동시성 문제 또는 DB 오류 발생
            log.error("통계 업데이트 중 오류 발생: Date={}, Error={}", date, e.getMessage(), e);
            throw e;
        }
    }
}
