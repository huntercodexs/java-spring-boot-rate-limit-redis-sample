package com.huntercodexs.api.ratelimit.service;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MessageProcessorService {

    @RateLimitServiceBus(limit = 5, per = 1, unit = TimeUnit.SECONDS)
    public void processMessage(ServiceBusReceivedMessage message) {
        System.out.println("Processing message: " + message.getBody());
    }
}