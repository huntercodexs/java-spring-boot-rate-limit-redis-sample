package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import com.huntercodexs.api.ratelimit.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    public RateLimitAspect(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Around("@annotation(com.huntercodexs.api.ratelimit.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        RateLimit annotation = method.getAnnotation(RateLimit.class);

        int limit = annotation.limit();
        int duration = annotation.duration();
        TimeUnit unit = annotation.unit();

        // Try to get identifier from request (e.g., client ID or IP)
        String identifier = getIdentifier();
        if (identifier == null) {
            identifier = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }

        String key = rateLimitService.buildKey(method.getName(), identifier, duration, unit);

        boolean allowed = rateLimitService.isAllowed(key, limit, duration, unit);
        if (!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded for " + identifier);
        }

        return pjp.proceed();
    }

    private String getIdentifier() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();

        if (attrs == null) return null;

        Object req = attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);

        if (req instanceof HttpServletRequest request) {

            String client = request.getHeader("X-Client-Id");
            if (client != null && !client.isBlank()) {
                return client;
            }

            String ip = request.getRemoteAddr();
            if (ip != null) {
                return ip;
            }
        }

        return null;
    }
}
