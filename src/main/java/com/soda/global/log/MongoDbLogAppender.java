package com.soda.global.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

@RequiredArgsConstructor
public class MongoDbLogAppender extends AppenderBase<ILoggingEvent> {

    private final LogRepository logRepository;

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!eventObject.getLevel().isGreaterOrEqual(ch.qos.logback.classic.Level.WARN)) {
            return;
        }

        StackTraceElement[] callerData = eventObject.getCallerData();
        String logger = eventObject.getLoggerName();
        String method = callerData.length > 0 ? callerData[0].getMethodName() : "unknown";
        int line = callerData.length > 0 ? callerData[0].getLineNumber() : -1;
        String message = MessageFormatter.arrayFormat(eventObject.getMessage(), eventObject.getArgumentArray()).getMessage();

        LogInfo log = LogInfo.builder()
                .log(String.format("[%s] %s %s.%s:%d - %s",
                        eventObject.getThreadName(),
                        eventObject.getLevel(),
                        logger,
                        method,
                        line,
                        message
                ))
                .time(LocalDateTime.ofInstant(Instant.ofEpochMilli(eventObject.getTimeStamp()), TimeZone.getDefault().toZoneId()))
                .build();

        try {
            logRepository.save(log);
        } catch (Exception e) {
            // Mongo 연결 오류 시 무시 (애플리케이션 죽지 않게)
            addError("Mongo 저장 실패", e);
        }
    }
}
