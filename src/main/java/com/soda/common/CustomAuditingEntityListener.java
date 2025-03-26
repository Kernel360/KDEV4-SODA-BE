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

public class CustomAuditingEntityListener {

    private static final ThreadLocal<Map<Object, Object>> ORIGINAL_STATE = ThreadLocal.withInitial(HashMap::new);

    @PostLoad
    public void postLoad(Object entity) {
        try {
            Map<String, Object> state = new HashMap<>();
            for (Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                state.put(field.getName(), field.get(entity));
            }
            ORIGINAL_STATE.get().put(entity, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        try {
            Map<String, Object> original = (Map<String, Object>) ORIGINAL_STATE.get().get(entity);

            boolean shouldUpdate = false;
            for (String targetFieldName : List.of("content", "title")) { // 여기에 추적할 필드를 명시
                Field field = entity.getClass().getDeclaredField(targetFieldName);
                field.setAccessible(true);
                Object currentValue = field.get(entity);
                Object originalValue = original.get(targetFieldName);
                if ((currentValue != null && !currentValue.equals(originalValue)) ||
                        (currentValue == null && originalValue != null)) {
                    shouldUpdate = true;
                    break;
                }
            }

            if (shouldUpdate) {
                Field updatedAtField = entity.getClass().getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(entity, LocalDateTime.now());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PrePersist
    public void prePersist(Object entity) {
        try {
            Field createdAtField = entity.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(entity, LocalDateTime.now());

            Field updatedAtField = entity.getClass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, LocalDateTime.now());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}