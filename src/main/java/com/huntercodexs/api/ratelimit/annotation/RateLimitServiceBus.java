package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimitServiceBus {
    int limit() default 5;
    int duration() default 10;
    TimeUnit unit() default TimeUnit.SECONDS;
    String keyParameterName() default "_MENSAGEM_INTEIRA_";
}
