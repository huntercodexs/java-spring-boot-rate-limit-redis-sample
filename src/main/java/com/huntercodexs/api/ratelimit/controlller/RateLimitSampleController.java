package com.huntercodexs.api.ratelimit.controlller;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitSampleController {

    @RateLimit(limit = 5, duration = 1) // 5 requests per minute
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Redis-backed rate limit!";
    }

    @RateLimit(limit = 3, duration = 30, unit = java.util.concurrent.TimeUnit.SECONDS)
    @GetMapping("/fast")
    public String fast() {
        return "3 requests every 30 seconds (shared across instances)";
    }
}

