package com.soda.global.log.systemLog.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<SystemLog, String> {
}
