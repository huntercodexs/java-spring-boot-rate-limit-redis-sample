package com.huntercodexs.api.ratelimit;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.aspect.RateLimitServiceBusAspect;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private RateLimitServiceBus rateLimitServiceBus;

    @InjectMocks
    private RateLimitServiceBusAspect rateLimitServiceBusAspect;

    @BeforeEach
    void setup() {

        // Mock Redis to return ValueOperations
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // JoinPoint simulates the method being called
        when(joinPoint.getSignature()).thenReturn(methodSignature);

        // Rate Limit basic configuration
        when(rateLimitServiceBus.limit()).thenReturn(2);
        when(rateLimitServiceBus.duration()).thenReturn(10);
        when(rateLimitServiceBus.unit()).thenReturn(TimeUnit.SECONDS);
    }

    @Test
    void should_allowRequest_when_consumerKeyIsValid() throws Throwable {

        // Simulate the Consumer method
        Method method = TestConsumer.class.getMethod("processMessage", TestMessage.class);
        when(methodSignature.getMethod()).thenReturn(method);

        // Simulate the arguments (the actual message)
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TestMessage("userX")});

        // Rate Limit configuration for the consumer
        when(rateLimitServiceBus.keyParameterName()).thenReturn("message");

        // Simulate the first call (increments to 1)
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // Action
        rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus);

        // Verification
        verify(redisTemplate, times(1)).expire(anyString(), eq(Duration.ofSeconds(10)));
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_throwException_when_consumerLimitExceeded() throws Throwable {

        // Simulate the Consumer method
        Method method = TestConsumer.class.getMethod("processMessage", TestMessage.class);
        when(methodSignature.getMethod()).thenReturn(method);

        // Simulate the arguments (the actual message)
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TestMessage("userY")});

        // Rate Limit configuration for the consumer
        when(rateLimitServiceBus.keyParameterName()).thenReturn("message");

        // Simulate the third call (increments to 3 > limit=2)
        when(valueOperations.increment(anyString())).thenReturn(3L);

        // Action and Verification
        assertThrows(RateLimitExceededException.class,
                () -> rateLimitServiceBusAspect.rateLimit(joinPoint, rateLimitServiceBus));

        // The proceed method should not be called
        verify(joinPoint, never()).proceed();
    }

    // Consumer simulation
    private static class TestConsumer {
        @RateLimitServiceBus(limit = 2, duration = 10, unit = TimeUnit.SECONDS, keyParameterName = "message")
        public void processMessage(TestMessage message) {}
    }

    // Message simulation
    private static class TestMessage {
        private String userId;
        public TestMessage(String userId) { this.userId = userId; }
        // The Aspect uses toString() if the object is passed as a simple argument.
        @Override
        public String toString() { return userId; }
    }
}
