package com.soda.project.application.stage.article;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCacheEvictionHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    public void evictMyArticlesCacheForUser(Long userId) {
        String cacheName = "myArticles";
        String keyPattern = cacheName + "::user:" + userId + ":*";

        log.info("사용자 ID {}의 '{}' 패턴에 해당하는 'myArticles' 캐시 항목 삭제를 시도합니다.", userId, keyPattern);

        Set<String> keysToDelete = new HashSet<>();
        try {
            redisTemplate.execute((RedisConnection connection) -> {
                // SCAN 옵션: 패턴 매칭, 한 번에 가져올 개수 (너무 크면 Redis 부하)
                ScanOptions options = ScanOptions.scanOptions().match(keyPattern).count(1000).build();
                try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                    while (cursor.hasNext()) {
                        keysToDelete.add(new String(cursor.next(), StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    log.error("Redis SCAN 중 오류 발생", e);
                    // SCAN 중에 예외가 발생해도, 이미 찾은 키들은 삭제 시도
                }
                return null;
            });

            if (!keysToDelete.isEmpty()) {
                Long deletedCount = redisTemplate.delete(keysToDelete);
                log.info("'{}' 패턴으로 조회된 {}개의 캐시 항목 중 {}개가 삭제되었습니다.", keyPattern, keysToDelete.size(), deletedCount);
            } else {
                log.info("'{}' 패턴에 해당하는 삭제할 캐시 항목이 없습니다.", keyPattern);
            }
        } catch (Exception e) {
            log.error("Redis 캐시 무효화 중 오류 발생 (userId: {}): {}", userId, e.getMessage(), e);
        }
    }

}
