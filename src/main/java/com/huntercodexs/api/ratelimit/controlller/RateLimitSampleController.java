package com.huntercodexs.api.ratelimit.controlller;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class RateLimitSampleController {

    @GetMapping("/test")
    @RateLimit(limit = 5, duration = 1, unit = TimeUnit.MINUTES) // exemplo: 5 requisições por 1 minuto por cliente
    public String test() {
        return "Dados liberados com sucesso!";
    }
}
