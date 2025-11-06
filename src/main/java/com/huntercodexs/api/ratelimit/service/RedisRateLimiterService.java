package com.huntercodexs.api.ratelimit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisRateLimiterService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean isAllowed(String key, int limit, long timeWindowSeconds) {
        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == null) {
            return true;
        }

        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(timeWindowSeconds));
        }

        return currentCount <= limit;
    }
}
