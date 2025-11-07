package com.huntercodexs.api.ratelimit.service;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final StatefulRedisConnection<String, String> connection;

    public RateLimitService(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    public boolean isAllowed(String key, int limit, int duration, TimeUnit unit) {
        RedisCommands<String, String> commands = connection.sync();
        long current = commands.incr(key);

        if (current == 1L) {
            // TTL for the key if it's newly created
            long seconds = unit.toSeconds(duration);
            commands.expire(key, seconds);
        }

        return current <= limit;
    }

    public String buildKey(String prefix, String identifier, int duration, TimeUnit unit) {
        long windowSeconds = unit.toSeconds(duration);
        return String.format("ratelimit:%s:%s:%d", prefix, identifier, windowSeconds);
    }
}
