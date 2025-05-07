package com.soda.project.domain.stats;

import com.querydsl.core.Tuple;
import com.soda.project.interfaces.dto.ProjectStatsCondition;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public interface ProjectStatsProvider {
    List<Tuple> findProjectCreationStats(LocalDate startDate, LocalDate endDate,
                                         ProjectStatsCondition.TimeUnit timeUnit);
}
