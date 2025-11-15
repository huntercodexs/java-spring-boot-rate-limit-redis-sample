package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RateLimitServiceBus {
    int limit(); // executions allowed
    int per(); // time window
    TimeUnit unit() default TimeUnit.SECONDS; // time unit
}
