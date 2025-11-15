package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitServiceBusAspect {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Around("@annotation(com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimitServiceBus annotation = method.getAnnotation(RateLimitServiceBus.class);

        String key = method.getDeclaringClass().getName() + "#" + method.getName();

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(annotation));

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        }

        throw new RateLimitExceededException("Too many requests - try again later");
    }

    private Bucket createBucket(RateLimitServiceBus annotation) {

        int limit = annotation.limit();
        int per = annotation.per();
        TimeUnit unit = annotation.unit();

        Duration duration = Duration.ofMillis(unit.toMillis(per));

        Refill refill = Refill.intervally(limit, duration);
        Bandwidth bandwidth = Bandwidth.classic(limit, refill);

        return Bucket.builder()
                .addLimit(bandwidth)
                .build();
    }
}