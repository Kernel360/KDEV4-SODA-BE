package com.soda.global.log.responsetime;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLoggingAspect {

    private final HttpServletRequest request;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            String requestUri = request.getRequestURI();
            String httpMethod = request.getMethod();
            if(end - start > 1000) {
                log.warn("[API Response Time] {} {} executed in {} ms", httpMethod, requestUri, (end - start));
            } else {
                log.info("[API Response Time] {} {} executed in {} ms", httpMethod, requestUri, (end - start));
            }
        }
    }
}
