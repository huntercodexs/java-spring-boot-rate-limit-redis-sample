package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    int limit();
    int duration();
    TimeUnit unit() default TimeUnit.MINUTES;
}

