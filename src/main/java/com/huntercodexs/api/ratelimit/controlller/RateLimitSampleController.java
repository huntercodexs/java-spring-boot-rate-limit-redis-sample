package com.huntercodexs.api.ratelimit.controlller;

import java.util.concurrent.TimeUnit;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimitSampleController {

    // Limite de 3 requisições a cada 10 segundos
    @GetMapping("/api/limitado")
    @RateLimit(limit = 3, duration = 10, unit = TimeUnit.SECONDS)
    public String limitedEndpoint() {
        return "Requisição permitida. Limite: 3/10s.";
    }

    // Endpoint sem Rate Limit para comparação
    @GetMapping("/api/publico")
    public String publicEndpoint() {
        return "Requisição permitida. Este endpoint não tem Rate Limit.";
    }
}
