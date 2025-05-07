package com.soda.project.interfaces.stats;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ProjectStatsCondition {

    @NotNull(message = "시작일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd 형식
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) // yyyy-MM-dd 형식
    private LocalDate endDate;

    @NotNull(message = "조회 단위는 필수입니다.")
    private TimeUnit timeUnit; // 조회 단위 (Enum: DAY, WEEK, MONTH)

    // 조회 단위 Enum
    public enum TimeUnit {
        DAY, WEEK, MONTH
    }
}
