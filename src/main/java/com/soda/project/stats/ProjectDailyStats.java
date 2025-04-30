package com.soda.project.stats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectDailyStats {

    @Id
    @Column(name = "stat_date", columnDefinition = "DATE") // 날짜를 기본 키로 사용
    private LocalDate statDate;

    @Column(nullable = false)
    private Long creationCount; // 해당 날짜에 생성된 프로젝트 수

    @Builder
    public ProjectDailyStats (LocalDate statDate, Long creationCount) {
        this.statDate = statDate;
        this.creationCount = creationCount;
    }

    // 생성 건수를 업데이트하는 메서드
    public void incrementCount() {
        this.creationCount++;
    }
}
