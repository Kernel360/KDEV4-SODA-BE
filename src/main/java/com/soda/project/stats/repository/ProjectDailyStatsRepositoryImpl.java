package com.soda.project.stats.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.soda.project.interfaces.dto.ProjectStatsCondition.TimeUnit;
import com.soda.project.stats.QProjectDailyStats;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectDailyStatsRepositoryImpl implements ProjectDailyStatsRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private final QProjectDailyStats stats = QProjectDailyStats.projectDailyStats;

    @Override
    public List<Tuple> findProjectCreationStats(LocalDate startDate, LocalDate endDate, TimeUnit timeUnit) {
        // 시간 단위별 그룹핑 및 날짜 포맷팅 표현식
        StringTemplate dateGroupExpression = switch (timeUnit) {
            // 일: yyyy-MM-dd 형식 그대로 사용
            case DAY -> Expressions.stringTemplate("DATE_FORMAT({0}, {1})", stats.statDate, "%Y-%m-%d");
            // 주: yyyy-ww 형식 (월요일 시작 기준)
            case WEEK -> Expressions.stringTemplate("DATE_FORMAT({0}, {1})", stats.statDate, "%x-%v"); // %x = 년도, %v = 주(01-53, 월요일 시작)
            // 월: yyyy-MM 형식
            case MONTH -> Expressions.stringTemplate("DATE_FORMAT({0}, {1})", stats.statDate, "%Y-%m");
        };

        return queryFactory
                .select(
                        dateGroupExpression.as("date_group"),
                        stats.creationCount.sum().as("count") // 일별 creationCount 를 합산
                )
                .from(stats)
                .where(
                        stats.statDate.goe(startDate),
                        stats.statDate.loe(endDate)
                )
                .groupBy(dateGroupExpression)
                .orderBy(dateGroupExpression.asc())
                .fetch();
    }
}
