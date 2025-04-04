package com.soda.global.log;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogRepository extends MongoRepository<LogInfo, String> {
    Page<LogInfo> findByTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<LogInfo> findByLogContainingAndTimeBetween(String search, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
