package com.soda.common;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PostLoad;
import org.springframework.data.annotation.LastModifiedDate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomAuditingEntityListener {

    private static final ThreadLocal<Map<Object, Map<String, Object>>> ORIGINAL_STATE = ThreadLocal.withInitial(HashMap::new);

    @PostLoad
    public void postLoad(Object entity) {
        Map<String, Object> trackedValues = new HashMap<>();
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(TrackUpdate.class)) {
                field.setAccessible(true);
                try {
                    trackedValues.put(field.getName(), field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        ORIGINAL_STATE.get().put(entity, trackedValues);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        Map<String, Object> originalValues = ORIGINAL_STATE.get().get(entity);
        boolean shouldUpdate = false;

        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(TrackUpdate.class)) {
                field.setAccessible(true);
                try {
                    Object currentValue = field.get(entity);
                    Object originalValue = originalValues.get(field.getName());
                    if (!Objects.equals(currentValue, originalValue)) {
                        shouldUpdate = true;
                        break;
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (shouldUpdate) {
            try {
                Field updatedAtField = getFieldIncludingSuper(entity.getClass(), "updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(entity, LocalDateTime.now());
            } catch (Exception e) {
                throw new RuntimeException("updatedAt 필드가 없거나 접근 불가합니다", e);
            }
        }
    }

    // 수정된 @PrePersist 부분
    @PrePersist
    public void prePersist(Object entity) {
        try {
            Field createdAtField = getFieldIncludingSuper(entity.getClass(), "createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, LocalDateTime.now());

            Field updatedAtField = getFieldIncludingSuper(entity.getClass(), "updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Field getFieldIncludingSuper(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException(fieldName + " 필드가 없거나 접근 불가합니다");
    }
}