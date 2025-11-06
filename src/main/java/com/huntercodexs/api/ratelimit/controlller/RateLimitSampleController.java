package com.huntercodexs.api.ratelimit.controlller;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitSampleController {

    @RateLimit(limit = 3, timeWindowSeconds = 10)
    @GetMapping("/test")
    public ResponseEntity<String> getLimitedData() {
        return ResponseEntity.ok("Dados liberados!");
    }
}
