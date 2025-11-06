package com.huntercodexs.api.ratelimit.controlller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitSampleController {

    @GetMapping("/api/test")
    public String test() {
        return "Request OK";
    }
}
