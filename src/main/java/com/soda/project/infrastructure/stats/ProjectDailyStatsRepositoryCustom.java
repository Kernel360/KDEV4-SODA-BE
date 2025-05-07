package com.soda.project.infrastructure.stats;

import com.querydsl.core.Tuple;
import com.soda.project.interfaces.stats.ProjectStatsCondition;

import java.time.LocalDate;
import java.util.List;

public interface ProjectDailyStatsRepositoryCustom {
    List<Tuple> findProjectCreationStats(LocalDate startDate, LocalDate endDate,
                                         ProjectStatsCondition.TimeUnit timeUnit);
}
