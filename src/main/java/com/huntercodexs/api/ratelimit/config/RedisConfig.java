package com.huntercodexs.api.ratelimit.config;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient() {
        var uri = RedisURI.Builder
                .redis("localhost", 6379)
                .withPassword("123@Mudar!".toCharArray())
                .build();

        return RedisClient.create(uri);
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisConnection<String, String> redisConnection(RedisClient redisClient) {
        return redisClient.connect(StringCodec.UTF8);
    }

    @Bean
    public LettuceBasedProxyManager<String> lettuceProxyManager(StatefulRedisConnection<String, String> connection) {
        LettuceBasedProxyManager.LettuceBasedProxyManagerBuilder<String> builder = null;
        return new LettuceBasedProxyManager<>(builder);
    }
}

