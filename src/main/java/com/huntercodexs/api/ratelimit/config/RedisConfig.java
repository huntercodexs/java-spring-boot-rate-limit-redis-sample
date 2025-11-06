package com.huntercodexs.api.ratelimit.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Value("${redis.username:}")
    private String redisUsername;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.timeout.seconds:5}")
    private long timeoutSeconds;

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        RedisURI.Builder builder = RedisURI.builder()
                .withHost(redisHost)
                .withPort(redisPort)
                .withTimeout(Duration.ofSeconds(timeoutSeconds));

        if (redisPassword != null && !redisPassword.isBlank()) {
            if (redisUsername != null && !redisUsername.isBlank()) {
                builder.withAuthentication(redisUsername, redisPassword);
            } else {
                builder.withPassword(redisPassword.toCharArray());
            }
        }

        RedisURI uri = builder.build();
        return RedisClient.create(uri);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> statefulRedisConnection(RedisClient redisClient) {
        return redisClient.connect();
    }
}

