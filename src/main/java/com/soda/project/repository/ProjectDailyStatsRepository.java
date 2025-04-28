package com.soda.project.repository;

import com.soda.project.entity.ProjectDailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProjectDailyStatsRepository extends JpaRepository<ProjectDailyStats, Long>, ProjectDailyStatsRepositoryCustom {
    Optional<ProjectDailyStats> findByStatDate(LocalDate date);
}
