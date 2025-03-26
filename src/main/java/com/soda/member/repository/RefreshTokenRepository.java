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

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void save(String authId, String refreshToken) {
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + authId,
                refreshToken,
                refreshTokenValidTime / 1000,
                TimeUnit.SECONDS
        );
    }

    public Optional<String> findByAuthId(String authId) {
        String token = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + authId);
        return Optional.ofNullable(token);
    }

    public void deleteByAuthId(String authId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + authId);
    }
}
