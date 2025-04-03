package com.soda.global.log;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<LogInfo, String> {
    LogInfo findLogInfoByTest(String test);
}
