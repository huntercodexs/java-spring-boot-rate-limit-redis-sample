package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBus {
    int limit();
    int duration();
    TimeUnit unit() default TimeUnit.MINUTES;

    // Optional parameter name for more specific rate limiting (e.g., user ID, API key)
    String keyParameterName();
}
