package com.soda.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenValidTime;

    public void save(String authId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refresh_token:" + authId,
                refreshToken,
                refreshTokenValidTime / 1000,
                TimeUnit.SECONDS
        );
    }

    public Optional<String> findByAuthId(String authId) {
        String token = redisTemplate.opsForValue().get("refresh_token:" + authId);
        return Optional.ofNullable(token);
    }

    public void deleteByAuthId(String authId) {
        redisTemplate.delete("refresh_token:" + authId);
    }
}
