package com.soda.global.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoAppenderRegistrar {

    private final LogRepository logRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void registerMongoAppender() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        MongoDbLogAppender mongoAppender = new MongoDbLogAppender(logRepository);
        mongoAppender.setName("MONGODB");
        mongoAppender.setContext(context);
        mongoAppender.start();

        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(mongoAppender);
    }
}
