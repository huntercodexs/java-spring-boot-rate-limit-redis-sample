package com.huntercodexs.api.ratelimit.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

// **Usar Annotation**
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** Limite de requisições permitidas no período. */
    int limit();

    /** Duração do período (time window). */
    int duration();

    /** Unidade de tempo para a duração. */
    TimeUnit unit() default TimeUnit.MINUTES;
}
