package com.huntercodexs.api.ratelimit;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import org.springframework.stereotype.Service;

@Service
public class ServiceBusServiceMock {

    @RateLimitServiceBus(limit = 3, duration = 10, unit = java.util.concurrent.TimeUnit.SECONDS, keyParameterName = "messageId")
    public String process(String messageId, String payload) {
        return "OK-" + messageId;
    }
}
