package com.soda.global.log.dataLog;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "data_log")
@Getter
@Builder
public class DataLog {
    @Id
    private String id;

    private String entityName;       // ex. "Article"
    private String entityId;         // ex. "123"
    private String action;           // CREATE, UPDATE, DELETE
    private String operator;         // 수정한 사람 (username 또는 system)
    private LocalDateTime timestamp;

    private Map<String, Object> beforeData; // UPDATE/DELETE 시 이전 값
    private Map<String, Object> afterData;  // CREATE/UPDATE 시 이후 값
}
