package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests was exceeded by %d %s.";

    private final RedisTemplate<String, Long> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        // Data Request Information
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ipAddress = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Redis Key Construction
        // Format: ratelimit:<IP_CLIENT>:<METHOD_NAME>
        String redisKey = String.format("ratelimit:%s:%s", ipAddress, method.getName());

        // Rate Limit Parameters
        int limit = rateLimit.limit();
        int duration = rateLimit.duration();
        TimeUnit unit = rateLimit.unit();

        // Redis Operations
        long durationInSeconds = TimeUnit.SECONDS.convert(duration, unit);
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // TTL Setup for the key on first increment
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(durationInSeconds));
        }

        // Limit Check
        if (currentCount > limit) {
            limitExceededAction(limit, duration, unit);
        }

        // Forward the request if within limit
        return joinPoint.proceed();
    }

    private void limitExceededAction(int limit, int duration, TimeUnit unit) {
        throw new RateLimitExceededException(String.format(MSG_RATE_LIMIT_EXCEEDED, limit, duration, unit.toString().toLowerCase()));
    }
}