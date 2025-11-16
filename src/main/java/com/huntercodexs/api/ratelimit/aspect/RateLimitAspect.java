package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.limit:0}")
    private int customLimit;

    @Value("${rate-limit.duration:0}")
    private int customDuration;

    @Value("${rate-limit.unit:minutes}")
    private String customUnit;

    @Value("${rate-limit.cache-prefix:ratelimit}")
    private String customPrefix;

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests was exceeded by %d %s.";

    private final RedisTemplate<String, Long> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        if (!rateLimitEnabled) {
            log.warn("Rate limiting is disabled via configuration.");
            return joinPoint.proceed();
        }

        // Data Request Information
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String ipAddress = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Redis Key Construction
        // Format: ratelimit:<IP_CLIENT>:<METHOD_NAME>
        String redisKey = String.format(customPrefix+":%s:%s", ipAddress, method.getName());

        // Redis Operations
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // Rate Limit Parameters
        int limit = rateLimit.limit();
        if (customLimit > 0) limit = customLimit;

        int duration = rateLimit.duration();
        if (customDuration > 0) duration = customDuration;

        // TTL Setup for the key on first increment
        TimeUnit unit = rateLimit.unit();

        if (customUnit.equalsIgnoreCase("SECONDS")) {
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
                unit = TimeUnit.SECONDS;
            }
        } else if (customUnit.equalsIgnoreCase("MINUTES")) {
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
                unit = TimeUnit.MINUTES;
            }
        } else if (customUnit.equalsIgnoreCase("HOURS")) {
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofHours(TimeUnit.HOURS.convert(duration, unit)));
                unit = TimeUnit.HOURS;
            }
        } else {
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
            }
        }

        log.info("Rate Limit Check - Key: {}, Count: {}, Limit: {}/{} {}", redisKey, currentCount, limit, duration, unit);

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