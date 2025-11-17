package com.huntercodexs.api.ratelimit;

import com.huntercodexs.api.ratelimit.aspect.RateLimitServiceBusAspect;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ServiceBusServiceMock.class})
public class RateLimitServiceBusAspectTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOps;

    private RateLimitServiceBusAspect aspect;

    private ServiceBusServiceMock consumerProxy;

    @BeforeEach
    void setup() {
        Mockito.reset(redisTemplate, valueOps);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        aspect = new RateLimitServiceBusAspect(redisTemplate);

        AspectJProxyFactory factory = new AspectJProxyFactory(new ServiceBusServiceMock());
        factory.addAspect(aspect);

        consumerProxy = factory.getProxy();
    }

    private void setPrivate(Object target, String fld, Object v) throws Exception {
        Field f = target.getClass().getDeclaredField(fld);
        f.setAccessible(true);
        f.set(target, v);
    }

    @Test
    void testWithinLimit() {
        when(valueOps.increment(anyString()))
                .thenReturn(1L)
                .thenReturn(2L);

        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

        assertEquals("OK-id-123", consumerProxy.process("id-123", "payload"));
        assertEquals("OK-id-123", consumerProxy.process("id-123", "payload"));
    }

    @Test
    void testExpireOnlyOnFirstIncrement() {
        when(valueOps.increment(anyString()))
                .thenReturn(1L)
                .thenReturn(2L);

        consumerProxy.process("id-xyz", "data");
        consumerProxy.process("id-xyz", "data");

        verify(redisTemplate, times(1))
                .expire(contains("id-xyz"), any(Duration.class));
    }

    @Test
    void testLimitExceeded() {
        when(valueOps.increment(anyString()))
                .thenReturn(1L)
                .thenReturn(2L)
                .thenReturn(3L)
                .thenReturn(4L);

        consumerProxy.process("999", "test");
        consumerProxy.process("999", "test");
        consumerProxy.process("999", "test");

        assertThrows(
                RateLimitExceededException.class,
                () -> consumerProxy.process("999", "test")
        );
    }

    @Test
    void testRateLimitDisabled() throws Exception {
        setPrivate(aspect, "rateLimitEnabled", false);

        when(valueOps.increment(anyString()))
                .thenReturn(1L);

        String r = consumerProxy.process("A1", "payload");

        assertEquals("OK-A1", r);

        verify(valueOps, never()).increment(anyString());
    }

    @Test
    void testOverrideLimit() throws Exception {
        setPrivate(aspect, "overrideLimit", 1);

        when(valueOps.increment(anyString()))
                .thenReturn(1L)
                .thenReturn(2L);

        consumerProxy.process("Z1", "payload");

        assertThrows(
                RateLimitExceededException.class,
                () -> consumerProxy.process("Z1", "payload")
        );
    }

    @Test
    void testCustomKeyParameterName() throws Exception {
        setPrivate(aspect, "overrideKeyParameter", "payload");

        when(valueOps.increment(contains("samplePayload")))
                .thenReturn(1L);

        String r = consumerProxy.process("sampleId", "samplePayload");

        assertEquals("OK-sampleId", r);
    }
}
