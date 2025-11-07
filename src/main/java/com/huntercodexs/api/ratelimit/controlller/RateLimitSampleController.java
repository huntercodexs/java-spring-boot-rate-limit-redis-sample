package com.huntercodexs.api.ratelimit.controlller;

import java.util.concurrent.TimeUnit;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitSampleController {

    @GetMapping("/api/limited")
    @RateLimit(limit = 3, duration = 10, unit = TimeUnit.SECONDS)
    public String limitedEndpoint() {
        return "Request Allowed. Limit: 3/10s.";
    }

    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "Request Allowed. This is a free endpoint.";
    }
}
