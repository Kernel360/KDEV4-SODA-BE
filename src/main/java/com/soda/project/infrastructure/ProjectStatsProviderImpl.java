package com.soda.project.infrastructure;

import com.querydsl.core.Tuple;
import com.soda.project.domain.stats.ProjectStatsProvider;
import com.soda.project.interfaces.dto.ProjectStatsCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProjectStatsProviderImpl implements ProjectStatsProvider {
    private final ProjectDailyStatsRepository projectDailyStatsRepository;

    @Override
    public List<Tuple> findProjectCreationStats(LocalDate startDate, LocalDate endDate, ProjectStatsCondition.TimeUnit timeUnit) {
        return projectDailyStatsRepository.findProjectCreationStats(startDate, endDate, timeUnit);
    }
}
