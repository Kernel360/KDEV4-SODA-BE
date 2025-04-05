package com.soda.global.log.systemLog.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "system_log")
@Getter
@Builder
public class SystemLog {
    @Id
    private String id;

    private String thread;
    private String level;
    private String logger;
    private String method;
    private int line;
    private String message;
    private LocalDateTime time;
}
