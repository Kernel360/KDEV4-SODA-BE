package com.soda.global.log;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "data_log") // 실제 몽고 DB 컬렉션 이름
@Getter
public class LogInfo {
    @Id
    private String id;
    private String log;
    private LocalDateTime time;

    @Builder
    public LogInfo(String id, String log, LocalDateTime time) {
        this.id = id;
        this.log = log;
        this.time = time;
    }
}