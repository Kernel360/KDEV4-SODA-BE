package com.soda.global.log.dataLog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EntityLogAspect {

    private final DataLogRepository dataLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationContext applicationContext;

    @AfterReturning(value = "@annotation(LoggableEntityAction)", returning = "result")
    public void logEntityAction(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LoggableEntityAction annotation = method.getAnnotation(LoggableEntityAction.class);

        String action = annotation.action(); // CREATE / UPDATE / DELETE

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String operator = "system";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            operator = auth.getName(); // JWT에서 추출한 유저 authId
        }

        Object[] args = joinPoint.getArgs();

        Map<String, Object> beforeData = null;
        Map<String, Object> afterData = convertToMap(result);

        if ("UPDATE".equals(action) || "DELETE".equals(action)) {
            Object entityId = getIdFromArgs(args);
            Object existing = loadEntityFromDb(result.getClass(), entityId);
            beforeData = convertToMap(existing);
        }

        DataLog dataLog = DataLog.builder()
                .entityName(result.getClass().getSimpleName())
                .entityId(extractId(result))
                .action(action)
                .operator(operator)
                .timestamp(LocalDateTime.now())
                .beforeData(beforeData)
                .afterData(afterData)
                .build();

        try {
            dataLogRepository.save(dataLog);
        } catch (Exception e) {
            log.error("데이터 로그 저장 실패", e);
        }
    }

    private Map<String, Object> convertToMap(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper.convertValue(obj, new TypeReference<Map<String, Object>>() {});
    }

    private Object getIdFromArgs(Object[] args) {
        return Arrays.stream(args)
                .filter(arg -> arg instanceof Long || arg instanceof String)
                .findFirst()
                .orElse(null);
    }

    private Object loadEntityFromDb(Class<?> entityClass, Object id) {
        String repoBeanName = entityClass.getSimpleName().replace("Dto", "") + "Repository";
        repoBeanName = Character.toLowerCase(repoBeanName.charAt(0)) + repoBeanName.substring(1);

        try {
            Object repository = applicationContext.getBean(repoBeanName);
            Method findById = repository.getClass().getMethod("findById", id.getClass());
            Object result = findById.invoke(repository, id);
            if (result instanceof java.util.Optional) {
                return ((java.util.Optional<?>) result).orElse(null);
            }
        } catch (Exception e) {
            log.error("엔티티 로딩 실패", e);
        }
        return null;
    }

    private String extractId(Object entity) {
        try {
            Field idField = Arrays.stream(entity.getClass().getDeclaredFields())
                    .filter(f -> f.getName().equalsIgnoreCase("id"))
                    .findFirst().orElse(null);

            if (idField == null) return null;
            idField.setAccessible(true);
            Object idValue = idField.get(entity);
            return idValue != null ? idValue.toString() : null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }
}