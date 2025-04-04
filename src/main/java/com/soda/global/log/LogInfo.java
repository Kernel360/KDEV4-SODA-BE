package com.soda.global.log;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "system_log")
public class LogInfo {

    @Id
    private String id;

    private String thread;
    private String level;
    private String logger;
    private String method;
    private int line;
    private String message;
    private LocalDateTime time;

    @Builder
    public LogInfo(String thread, String level, String logger, String method, int line, String message, LocalDateTime time) {
        this.thread = thread;
        this.level = level;
        this.logger = logger;
        this.method = method;
        this.line = line;
        this.message = message;
        this.time = time;
    }
}
