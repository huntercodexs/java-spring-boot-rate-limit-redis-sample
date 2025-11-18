package com.huntercodexs.api.ratelimit.simulation.controller;

import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.simulation.dto.ProcessMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ServiceBusConsumerSimulation {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerSimulation.class);

    @PostMapping("/simulate-queue-process")
    @RateLimitServiceBus(limit = 5, duration = 10, unit = TimeUnit.SECONDS, keyParameterName = "message")
    public ResponseEntity<String> processMessage(@RequestBody ProcessMessage message) {
        log.info("Processing message for UserID: {}", message.getUserId());
        return ResponseEntity.ok("Message processed successfully for userId: " + message.getUserId());
    }
}
