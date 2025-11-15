package com.huntercodexs.api.ratelimit.mock.controller;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.dto.ProcessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ServiceBusConsumerMock {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerMock.class);

    /**
     * MOCK endpoint that simulates processing messages from a Service Bus queue with rate limiting.
     * <br>
     * Important: keyParameterName should match a field inside ProcessMessage class,
     * it can be userId, orderId, etc... inside the message payload.
     */
    @PostMapping("/simulate-queue-process")
    @RateLimitServiceBus(limit = 2, duration = 15, unit = TimeUnit.SECONDS, keyParameterName = "message")
    public ResponseEntity<String> processMessage(@RequestBody ProcessMessage message) {
        log.info("Processing message for UserID: {}", message.getUserId());

        /* Your code here !*/

        return ResponseEntity.ok("Message processed successfully for userId: " + message.getUserId());
    }
}
