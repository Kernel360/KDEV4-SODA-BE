package com.soda.project.stats.repository;

import com.soda.project.domain.ProjectDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProjectDailyStatsRepository extends JpaRepository<ProjectDailyStats, Long>, ProjectDailyStatsRepositoryCustom {
    Optional<ProjectDailyStats> findByStatDate(LocalDate date);
}
