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

    /**
     * Retorna true se permitido, false se limite excedido.
     *
     * Implementação: INCR key -> se 1 então EXPIRE(key, duration)
     */
    public boolean isAllowed(String key, int limit, int duration, TimeUnit unit) {
        RedisCommands<String, String> commands = connection.sync();
        long current = commands.incr(key);
        if (current == 1L) {
            // define TTL na primeira contagem
            long seconds = unit.toSeconds(duration);
            commands.expire(key, seconds);
        }
        return current <= limit;
    }

    /**
     * uma utilidade para montar a chave TTL-friendly (opcional de usar)
     */
    public String buildKey(String prefix, String identifier, int duration, TimeUnit unit) {
        // prefix: ratelimit, identifier: cliente/metodo, duration: window
        long windowSeconds = unit.toSeconds(duration);
        return String.format("ratelimit:%s:%s:%d", prefix, identifier, windowSeconds);
    }
}
