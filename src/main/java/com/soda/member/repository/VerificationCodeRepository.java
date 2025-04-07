package com.soda.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class VerificationCodeRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void saveVerificationCode(String key, String code, long expirationTime) {
        deleteVerificationCode(key);
        redisTemplate.opsForValue().set(key, code, expirationTime, TimeUnit.SECONDS);
    }

    public String findVerificationCode(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteVerificationCode(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasVerificationCode(String key) {
        return redisTemplate.hasKey(key);
    }
}
