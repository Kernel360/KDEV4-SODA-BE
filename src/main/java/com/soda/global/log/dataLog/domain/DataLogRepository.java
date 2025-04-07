package com.soda.global.log.dataLog.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataLogRepository extends MongoRepository<DataLog, String>, DataLogCustomRepository {
}

