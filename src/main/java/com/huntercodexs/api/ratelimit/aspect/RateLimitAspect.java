package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import com.huntercodexs.api.ratelimit.service.RedisRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedisRateLimiterService rateLimiterService;

    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String clientIdentifier = request.getRemoteAddr();
        String redisKey = "ratelimit:" + clientIdentifier + ":" + method.getName();

        boolean allowed = rateLimiterService.isAllowed(
                redisKey,
                rateLimit.limit(),
                rateLimit.timeWindowSeconds()
        );

        if (!allowed) {
            throw new RateLimitExceededException("Too Many Requests");
        }

        return joinPoint.proceed();
    }
}
