package com.soda.member.domain;

import com.soda.member.interfaces.dto.CompanyCreationStatRaw;
import com.soda.member.interfaces.dto.CompanyCreationTrend;
import com.soda.member.interfaces.dto.company.CompanyTrendSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyStatsService {
    private final CompanyProvider companyProvider;

    public List<CompanyCreationTrend> getCompanyCreationTrend(CompanyTrendSearchCondition condition) {
        LocalDate startDate = condition.getStartDate();
        LocalDate endDate = condition.getEndDate();
        StatisticsUnit unit = condition.getUnit() != null ? condition.getUnit() : StatisticsUnit.DAY;

        // LocalDate를 LocalDateTime으로 변환 (시작일은 00:00:00, 종료일은 23:59:59)
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<CompanyCreationStatRaw> rawStats = companyProvider.countCompaniesByDayRaw(startDateTime, endDateTime);

        // 날짜별 카운트 맵 생성
        Map<String, Long> statsMap = rawStats.stream()
                .collect(Collectors.toMap(
                        stat -> unit.formatPeriod(stat.getDate()),
                        CompanyCreationStatRaw::count,
                        (v1, v2) -> v1 // 중복 키가 있을 경우 첫 번째 값 유지
                ));

        // 전체 기간에 대한 데이터 생성
        List<CompanyCreationTrend> result = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            String period = unit.formatPeriod(currentDate);
            long count = statsMap.getOrDefault(period, 0L);
            result.add(new CompanyCreationTrend(period, count));

            // 다음 기간으로 이동
            switch (unit) {
                case DAY -> currentDate = currentDate.plusDays(1);
                case WEEK -> currentDate = currentDate.plusWeeks(1);
                case MONTH -> currentDate = currentDate.plusMonths(1);
            }
        }

        return result;
    }
}