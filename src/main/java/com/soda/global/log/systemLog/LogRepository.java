package com.soda.global.log.systemLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface LogRepository extends MongoRepository<SystemLog, String> {
    Page<SystemLog> findByTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<SystemLog> findByLevelAndTimeBetween(String level, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<SystemLog> findByLoggerContainingAndTimeBetween(String logger, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
