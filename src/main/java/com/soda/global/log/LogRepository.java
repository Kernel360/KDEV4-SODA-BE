package com.soda.global.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface LogRepository extends MongoRepository<LogInfo, String> {
    Page<LogInfo> findByTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<LogInfo> findByLevelAndTimeBetween(String level, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<LogInfo> findByLoggerContainingAndTimeBetween(String logger, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
