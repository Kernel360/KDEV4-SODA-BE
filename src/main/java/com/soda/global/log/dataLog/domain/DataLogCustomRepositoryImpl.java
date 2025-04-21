package com.soda.global.log.dataLog.domain;

import com.soda.global.log.dataLog.dto.DataLogSearchRequest;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DataLogCustomRepositoryImpl implements DataLogCustomRepository {

    private final MongoTemplate mongoTemplate;

    public DataLogCustomRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<DataLog> searchLogs(DataLogSearchRequest condition, Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "timestamp")
            );
        }

        Query pagedQuery = new Query().with(pageable);
        Query countQuery = new Query();

        applyCriteria(pagedQuery, condition);
        applyCriteria(countQuery, condition);

        long total = mongoTemplate.count(countQuery, DataLog.class);
        List<DataLog> logs = mongoTemplate.find(pagedQuery, DataLog.class);

        return new PageImpl<>(logs, pageable, total);
    }

    private void applyCriteria(Query query, DataLogSearchRequest condition) {
        if (condition.getAction() != null && !condition.getAction().isBlank()) {
            query.addCriteria(Criteria.where("action").is(condition.getAction().toUpperCase()));
        }

        if (condition.getOperator() != null && !condition.getOperator().isBlank()) {
            query.addCriteria(Criteria.where("operator").regex(condition.getOperator(), "i"));
        }

        if (condition.getEntityName() != null && !condition.getEntityName().isBlank()) {
            query.addCriteria(Criteria.where("entityName").regex(condition.getEntityName(), "i"));
        }

        if (condition.getFrom() != null && condition.getTo() != null) {
            query.addCriteria(Criteria.where("timestamp").gte(condition.getFrom()).lte(condition.getTo()));
        }
    }
}

