package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitServiceBusAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusAspect.class);

    private static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests exceeded for key '%s' in %d %s.";

    private final RedisTemplate<String, Long> redisTemplate;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();

    @Around("@annotation(RateLimitServiceBus)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimitServiceBus rateLimitServiceBus) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // Get method arguments (message, headers, etc.)

        // Getting Rate Limit Key Value
        String keyParameterName = rateLimitServiceBus.keyParameterName();
        Object rateLimitKeyValue = findParameterValue(method, args, keyParameterName);

        if (rateLimitKeyValue == null) {
            // If the key parameter is not found or is null, handle accordingly.
            log.warn("Alert: Rate Limit key parameter not found or is null. Request allowed.");
            return joinPoint.proceed();
        }

        // Building the Redis Key - Format: rateLimitServiceBusKeyName:consumer:<METHOD_NAME>:<KEY_VALUE>
        String redisKey = String.format("rateLimitServiceBusKeyName:consumer:%s:%s", method.getName(), rateLimitKeyValue);

        // Get values from annotation
        int limit = rateLimitServiceBus.limit();
        int duration = rateLimitServiceBus.duration();
        TimeUnit unit = rateLimitServiceBus.unit();
        long durationInSeconds = TimeUnit.SECONDS.convert(duration, unit);

        // Rate limiting logic
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) return joinPoint.proceed();

        if (currentCount == 1) redisTemplate.expire(redisKey, Duration.ofSeconds(durationInSeconds));

        // Check if limit exceeded
        if (currentCount > limit) {
            // When the limit is exceeded, throw an exception, this exception will be handled globally.
            // In this case, we throw RateLimitExceededException and the Spring Cloud Stream/ASB binder
            // will be able to catch it and not acknowledge the message, allowing for reprocessing later.
            limitExceededAction(rateLimitKeyValue, limit, duration, unit);
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    private void limitExceededAction(Object rateLimitKeyValue, int limit, int duration, TimeUnit unit) {
        throw new RateLimitExceededException(String.format(
                MSG_RATE_LIMIT_EXCEEDED, limit, rateLimitKeyValue, duration, unit.toString().toLowerCase()));
    }

    /**
     * Makes the mapping from parameter name to actual argument value.
     * Requires the -parameters flag in the compiler.
     */
    private Object findParameterValue(Method method, Object[] args, String parameterName) {
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(parameterName)) {
                    return args[i];
                }
            }
        }
        return null;
    }
}