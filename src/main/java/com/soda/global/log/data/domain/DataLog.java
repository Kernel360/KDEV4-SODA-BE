package com.soda.global.log.data.domain;

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

    private String entityName;
    private String entityId;
    private String action;           // CREATE, UPDATE, DELETE
    private String operator;         // 데이터 생성/수정/삭제자 (system or authId)
    private LocalDateTime timestamp;

    private Map<String, Object> beforeData; // UPDATE/DELETE 시 이전 값
    private Map<String, Object> afterData;  // CREATE/UPDATE 시 이후 값
    private Map<String, Object> diff;       // UPDATE 시 변경된 필드 정보

    private String beforeDataText;
    private String afterDataText;
}
