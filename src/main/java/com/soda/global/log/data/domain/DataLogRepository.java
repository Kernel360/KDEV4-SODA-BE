package com.soda.global.log.data.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataLogRepository extends MongoRepository<DataLog, String>, DataLogCustomRepository {
}

