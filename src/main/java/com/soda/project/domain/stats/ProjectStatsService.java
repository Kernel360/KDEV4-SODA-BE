package com.soda.project.domain.stats;

import com.querydsl.core.Tuple;
import com.soda.project.interfaces.dto.ProjectStatsCondition;
import com.soda.project.interfaces.dto.ProjectStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectStatsService {
    private final ProjectStatsProvider projectStatsProvider;

    public ProjectStatsResponse getProjectCreationTrend(ProjectStatsCondition request) {
        log.debug("[StatsService] 프로젝트 생성 통계 데이터 생성 시작: 조건={}", request);

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Tuple> statsData = projectStatsProvider.findProjectCreationStats(startDate, endDate, request.getTimeUnit());
        log.debug("[StatsService] 프로젝트 생성 통계 DB 조회 완료: {}개의 데이터", statsData.size());

        // 조회 결과를 Map 변환
        Map<String, Long> statsMap = statsData.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(0, String.class),
                        tuple -> tuple.get(1, Long.class) != null ? tuple.get(1, Long.class) : 0L,
                        (existing, replacement) -> existing
                ));

        // 조회 기간 내 모든 날짜 생성 및 0으로 채우기
        List<ProjectStatsResponse.DataPoint> trendData = generateFullTrendData(startDate, endDate, request.getTimeUnit(), statsMap);
        log.debug("[StatsService] 누락 기간 0 처리 및 최종 DataPoint 리스트 생성 완료. Size: {}", trendData.size());

        ProjectStatsResponse response = ProjectStatsResponse.from(request, trendData);
        log.info("[StatsService] 프로젝트 생성 통계 응답 생성 완료");
        return response;
    }

    private List<ProjectStatsResponse.DataPoint> generateFullTrendData(LocalDate startDate, LocalDate endDate,
                                                                       ProjectStatsCondition.TimeUnit timeUnit, Map<String, Long> statsMap) {
        List<ProjectStatsResponse.DataPoint> fullTrend = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        WeekFields weekFields = WeekFields.of(Locale.KOREA);

        switch (timeUnit) {
            case DAY:
                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    String dateKey = currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    long count = statsMap.getOrDefault(dateKey, 0L);
                    fullTrend.add(ProjectStatsResponse.DataPoint.builder().date(dateKey).count(count).build());
                    currentDate = currentDate.plusDays(1);
                }
                break;
            case WEEK:
                LocalDate currentWeekStart = startDate.with(weekFields.dayOfWeek(), 1);
                LocalDate endWeekStart = endDate.with(weekFields.dayOfWeek(), 1);
                DateTimeFormatter weekRepoFormatter = DateTimeFormatter.ofPattern("yyyy-ww", Locale.KOREA);

                while (!currentWeekStart.isAfter(endWeekStart)) {
                    String repositoryDateKey = currentWeekStart.format(weekRepoFormatter);
                    int year = currentWeekStart.getYear();
                    int month = currentWeekStart.getMonthValue();
                    int weekOfMonth = currentWeekStart.get(weekFields.weekOfMonth());
                    String displayDateString = String.format("%d년 %d월 %d주차", year, month, weekOfMonth);
                    long count = statsMap.getOrDefault(repositoryDateKey, 0L);
                    fullTrend.add(ProjectStatsResponse.DataPoint.builder().date(displayDateString).count(count).build());
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;
            case MONTH:
                YearMonth currentMonth = YearMonth.from(startDate);
                YearMonth endYearMonth = YearMonth.from(endDate);

                while (!currentMonth.isAfter(endYearMonth)) {
                    String dateKey = currentMonth.format(monthFormatter);
                    long count = statsMap.getOrDefault(dateKey, 0L);
                    fullTrend.add(ProjectStatsResponse.DataPoint.builder().date(dateKey).count(count).build());
                    currentMonth = currentMonth.plusMonths(1);
                }
                break;
            default:
                log.warn("[StatsService] 지원하지 않는 TimeUnit: {}", timeUnit);
                break;
        }
        return fullTrend;
    }
}
