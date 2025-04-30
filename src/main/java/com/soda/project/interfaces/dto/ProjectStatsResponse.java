package com.soda.project.interfaces.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ProjectStatsResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatsCondition.TimeUnit timeUnit;
    private List<DataPoint> trend; // 추이 데이터 리스트

    @Getter
    @Builder
    public static class DataPoint {
        private String date; // 날짜 또는 주/월 시작일 (문자열)
        private long count;  // 해당 기간 생성 건수
    }

    public static ProjectStatsResponse from(ProjectStatsCondition request, List<DataPoint> trendData) {
        return ProjectStatsResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .timeUnit(request.getTimeUnit())
                .trend(trendData)
                .build();
    }
}
