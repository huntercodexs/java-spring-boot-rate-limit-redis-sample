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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${rate-limit-service-bus.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit-service-bus.limit:0}")
    private int customLimit;

    @Value("${rate-limit-service-bus.duration:0}")
    private int customDuration;

    @Value("${rate-limit-service-bus.unit:seconds}")
    private String customUnit;

    @Value("${rate-limit-service-bus.cache-prefix:rateLimitServiceBusDefaultKeyName}")
    private String customPrefix;

    @Value("${rate-limit-service-bus.key-parameter:}")
    private String customerKeyParameter;

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusAspect.class);

    private static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests exceeded for key '%s' in %d %s.";

    private final RedisTemplate<String, Long> redisTemplate;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();

    @Around("@annotation(rateLimitServiceBus)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimitServiceBus rateLimitServiceBus) throws Throwable {

        if (!rateLimitEnabled) {
            log.warn("Rate limiting service bus is disabled via configuration.");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // Get method arguments (message, headers, etc.)

        // Getting Rate Limit Key Value
        String keyParameterName = rateLimitServiceBus.keyParameterName();
        if (customerKeyParameter != null && !customerKeyParameter.isEmpty()) keyParameterName = customerKeyParameter;

        Object rateLimitKeyValue = findParameterValue(method, args, keyParameterName);

        if (rateLimitKeyValue == null) {
            // If the key parameter is not found or is null, handle accordingly.
            log.warn("Alert: Rate Limit key parameter not found or is null. Request allowed.");
            return joinPoint.proceed();
        }

        // Building the Redis Key - Format: rateLimitServiceBusKeyName:consumer:<METHOD_NAME>:<KEY_VALUE>
        String redisKey = String.format(customPrefix+":consumer:%s:%s", method.getName(), rateLimitKeyValue);

        // Rate limiting logic
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // Get values from annotation
        int limit = rateLimitServiceBus.limit();
        if (customLimit > 0) limit = customLimit;

        int duration = rateLimitServiceBus.duration();
        if (customDuration > 0) duration = customDuration;

        // TTL Setup for the key on first increment
        TimeUnit unit = rateLimitServiceBus.unit();

        if (customUnit.equalsIgnoreCase("SECONDS")) {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase("MINUTES")) {
            if (currentCount == 1) {
                unit = TimeUnit.MINUTES;
                redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase("HOURS")) {
            if (currentCount == 1) {
                unit = TimeUnit.HOURS;
                redisTemplate.expire(redisKey, Duration.ofHours(TimeUnit.HOURS.convert(duration, unit)));
            }
        } else {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        }

        log.info("Rate Limit Service Bus Check - Key: {}, Count: {}, Limit: {}/{} {}", redisKey, currentCount, limit, duration, unit);

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