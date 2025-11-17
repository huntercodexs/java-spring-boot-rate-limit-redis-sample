package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBus {
    int limit() default 10;
    int duration() default 50;
    TimeUnit unit() default TimeUnit.SECONDS;

    // Optional parameter name for more specific rate limiting (e.g., user ID, API key)
    String keyParameterName() default "message";
}
