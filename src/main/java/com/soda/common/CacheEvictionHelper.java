package com.soda.common;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheEvictionHelper {

    private final CacheManager cacheManager;

    public void evictMyRequestPage0Size3(Long memberId) {
        Cache cache = cacheManager.getCache("myRequests");
        if (cache != null) {
            String key = "member:" + memberId + ":page:0:size:3";
            cache.evictIfPresent(key);
        }
    }
}