package com.soda.project.stats.repository;

import com.querydsl.core.Tuple;
import com.soda.project.dto.ProjectStatsCondition;

import java.time.LocalDate;
import java.util.List;

public interface ProjectDailyStatsRepositoryCustom {
    List<Tuple> findProjectCreationStats(LocalDate startDate, LocalDate endDate,
                                         ProjectStatsCondition.TimeUnit timeUnit);
}
