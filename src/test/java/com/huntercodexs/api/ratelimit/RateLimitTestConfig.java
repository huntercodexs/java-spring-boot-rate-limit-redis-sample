package com.huntercodexs.api.ratelimit;

import com.huntercodexs.api.ratelimit.aspect.RateLimitServiceBusAspect;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
class RateLimitTestConfig {

    @Bean
    public RedisTemplate<String, Long> redisTemplate() {
        return Mockito.mock(RedisTemplate.class);
    }

    @Bean
    public RateLimitServiceBusAspect rateLimitServiceBusAspect(RedisTemplate<String, Long> redisTemplate) {
        return new RateLimitServiceBusAspect(
                redisTemplate
        );
    }
}

