package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
    private RateLimit rateLimit;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    private static final String TEST_IP = "192.168.1.100";
    private static final String METHOD_NAME = "testEndpoint";
    private static final String EXPECTED_KEY_PREFIX = "ratelimit:" + TEST_IP + ":" + METHOD_NAME;

    @BeforeEach
    void setup() throws NoSuchMethodException {
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitEnabled", true);
        ReflectionTestUtils.setField(rateLimitAspect, "overrideLimit", 0);
        ReflectionTestUtils.setField(rateLimitAspect, "overrideDuration", 0);
        ReflectionTestUtils.setField(rateLimitAspect, "overrideUnit", "MINUTES");
        ReflectionTestUtils.setField(rateLimitAspect, "customPrefix", "ratelimit");
    }

    @Test
    void should_allowRequest_andSetTTL_onFirstCall() throws Throwable {
        makeWay();
        when(valueOperations.increment(any())).thenReturn(1L);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        verify(valueOperations, times(1)).increment(any());
        verify(redisTemplate, times(1)).expire(any(), any());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_allowRequest_andNotSetTTL_onSubsequentCalls() throws Throwable {
        makeWay();
        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(3L);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        verify(valueOperations, times(1)).increment(anyString());
        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_throwException_when_limitExceeded() throws Throwable {
        makeWay();
        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(6L);

        assertThrows(RateLimitExceededException.class,
                () -> rateLimitAspect.rateLimit(joinPoint, rateLimit));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void should_disableRateLimit_when_rateLimitEnabledIsFalse() throws Throwable {
        ReflectionTestUtils.setField(rateLimitAspect, "rateLimitEnabled", false);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        verify(valueOperations, never()).increment(anyString());
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void should_useOverrideLimit_when_OverrideLimitIsSet() throws Throwable {
        makeWay();
        ReflectionTestUtils.setField(rateLimitAspect, "overrideLimit", 2);

        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(3L);

        assertThrows(RateLimitExceededException.class,
                () -> rateLimitAspect.rateLimit(joinPoint, rateLimit));

        verify(joinPoint, never()).proceed();
    }

    @Test
    void should_useOverrideDurationAndUnit_when_OverrideUnitIsMINUTES() throws Throwable {
        makeWay();
        ReflectionTestUtils.setField(rateLimitAspect, "overrideDuration", 1);
        ReflectionTestUtils.setField(rateLimitAspect, "overrideUnit", "MINUTES");

        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(1L);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        verify(redisTemplate, times(1)).expire(eq(EXPECTED_KEY_PREFIX), eq(Duration.ofMinutes(1)));
    }

    @Test
    void should_useOverrideDurationAndUnit_when_OverrideUnitIsHOURS() throws Throwable {
        makeWay();
        ReflectionTestUtils.setField(rateLimitAspect, "overrideDuration", 2);
        ReflectionTestUtils.setField(rateLimitAspect, "overrideUnit", "HOURS");

        when(valueOperations.increment(eq(EXPECTED_KEY_PREFIX))).thenReturn(1L);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        verify(redisTemplate, times(1)).expire(eq(EXPECTED_KEY_PREFIX), eq(Duration.ofHours(2)));
    }

    @Test
    void should_useCustomPrefix_when_customPrefixIsSet() throws Throwable {
        makeWay();
        String customPrefix = "API_RATE_LIMIT";
        ReflectionTestUtils.setField(rateLimitAspect, "customPrefix", customPrefix);

        when(valueOperations.increment(anyString())).thenReturn(1L);

        rateLimitAspect.rateLimit(joinPoint, rateLimit);

        String expectedKey = customPrefix + ":" + TEST_IP + ":" + METHOD_NAME;
        verify(valueOperations, times(1)).increment(eq(expectedKey));
    }

    private static class TestController {
        @RateLimit(limit = 5, duration = 60, unit = TimeUnit.SECONDS)
        public void testEndpoint() {}
    }

    private void makeWay() throws NoSuchMethodException {
        when(request.getRemoteAddr()).thenReturn(TEST_IP);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        Method method = TestController.class.getMethod(METHOD_NAME);
        when(methodSignature.getMethod()).thenReturn(method);

        when(rateLimit.limit()).thenReturn(5);
        when(rateLimit.duration()).thenReturn(60);
        when(rateLimit.unit()).thenReturn(TimeUnit.SECONDS);
    }
}
