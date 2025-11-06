package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
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

@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Around("@annotation(com.example.demo.ratelimit.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // unique key per method
        String key = method.getDeclaringClass().getName() + "#" + method.getName();

        Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(rateLimit));

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            throw new RateLimitExceededException("Too many requests - try again later");
        }
    }

    private Bucket createBucket(RateLimit rateLimit) {
        Duration refillDuration = Duration.ofMillis(rateLimit.unit().toMillis(rateLimit.duration()));
        Refill refill = Refill.intervally(rateLimit.limit(), refillDuration);
        Bandwidth limit = Bandwidth.classic(rateLimit.limit(), refill);
        return Bucket.builder().addLimit(limit).build();
    }
}
