package com.soda.global.log.systemLog;

import ch.qos.logback.classic.Level;
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
        if (!eventObject.getLevel().isGreaterOrEqual(Level.INFO)) {
            return;
        }

        StackTraceElement[] callerData = eventObject.getCallerData();
        String logger = eventObject.getLoggerName();
        String method = callerData.length > 0 ? callerData[0].getMethodName() : "unknown";
        int line = callerData.length > 0 ? callerData[0].getLineNumber() : -1;
        String message = MessageFormatter.arrayFormat(eventObject.getMessage(), eventObject.getArgumentArray()).getMessage();

        SystemLog log = SystemLog.builder()
                .thread(eventObject.getThreadName())
                .level(eventObject.getLevel().toString())
                .logger(logger)
                .method(method)
                .line(line)
                .message(message)
                .time(LocalDateTime.ofInstant(Instant.ofEpochMilli(eventObject.getTimeStamp()), TimeZone.getDefault().toZoneId()))
                .build();

        try {
            logRepository.save(log);
        } catch (Exception e) {
            // Mongo 저장 실패해도 애플리케이션은 계속 작동하게
            addError("MongoDB 로그 저장 실패", e);
        }
    }
}
