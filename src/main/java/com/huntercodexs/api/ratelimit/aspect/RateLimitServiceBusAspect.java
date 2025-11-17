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
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitServiceBusAspect {

    @Value("${rate-limit-service-bus.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit-service-bus.limit:0}")
    private int overrideLimit;

    @Value("${rate-limit-service-bus.duration:0}")
    private int overrideDuration;

    @Value("${rate-limit-service-bus.unit:SECONDS}")
    private String overrideUnit;

    @Value("${rate-limit-service-bus.cache-prefix:rateLimitServiceBusDefaultKeyName}")
    private String prefix;

    @Value("${rate-limit-service-bus.key-parameter:}")
    private String overrideKeyParameter;

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusAspect.class);

    private final RedisTemplate<String, Long> redisTemplate;
    private final ParameterNameDiscoverer nameDiscoverer = new StandardReflectionParameterNameDiscoverer();

    @Around("@annotation(rateLimit)")
    public Object applyRateLimit(ProceedingJoinPoint joinPoint, RateLimitServiceBus rateLimit) throws Throwable {

        if (!rateLimitEnabled) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Object[] args = joinPoint.getArgs();

        // Determine key parameter
        String keyParamName = (overrideKeyParameter != null && !overrideKeyParameter.isBlank())
                ? overrideKeyParameter
                : rateLimit.keyParameterName();

        Object keyValue = resolveMethodArg(method, args, keyParamName);
        if (keyValue == null) {
            return joinPoint.proceed();
        }

        String redisKey = "%s:consumer:%s:%s".formatted(
                prefix, method.getName(), keyValue.toString()
        );

        // increment counter
        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            return joinPoint.proceed();
        }

        // resolve configs
        int limit = overrideLimit > 0 ? overrideLimit : rateLimit.limit();
        int duration = overrideDuration > 0 ? overrideDuration : rateLimit.duration();
        TimeUnit unit = resolveTimeUnit(overrideUnit, rateLimit.unit());

        // define TTL only on first request
        if (count == 1) {
            redisTemplate.expire(redisKey, Duration.ofMillis(unit.toMillis(duration)));
        }

        log.info("ServiceBus RateLimit Key={} Count={} Limit={}/{} {}", redisKey, count, limit, duration, unit);

        // limit exceeded
        if (count > limit) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded: key=%s limit=%d per %d %s"
                            .formatted(keyValue, limit, duration, unit)
            );
        }

        return joinPoint.proceed();
    }

    private TimeUnit resolveTimeUnit(String overrideUnit, TimeUnit annotationUnit) {
        if (overrideUnit == null || overrideUnit.isBlank()) {
            return annotationUnit;
        }

        return switch (overrideUnit.trim().toUpperCase(Locale.ROOT)) {
            case "SECONDS", "SEC", "S" -> TimeUnit.SECONDS;
            case "MINUTES", "MIN", "M" -> TimeUnit.MINUTES;
            case "HOURS", "HOUR", "H" -> TimeUnit.HOURS;
            default -> annotationUnit;
        };
    }

    private Object resolveMethodArg(Method method, Object[] args, String paramName) {
        if (paramName == null || paramName.isBlank()) return null;

        String[] names = nameDiscoverer.getParameterNames(method);
        if (names == null) return null;

        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(paramName)) {
                return args[i];
            }
        }
        return null;
    }
}