package com.huntercodexs.api.ratelimit;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.huntercodexs.api.ratelimit.exception.RateLimitExceededException;
import com.huntercodexs.api.ratelimit.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RateLimitSpringContextTest {

    @Autowired
    private MessageProcessorService service;

    @Test
    public void testRateLimitInsideSpringContext() {
        ServiceBusReceivedMessage msg = Mockito.mock(ServiceBusReceivedMessage.class);

        for (int i = 0; i < 5; i++) {
            Assertions.assertDoesNotThrow(() -> service.processMessage(msg));
        }

        Assertions.assertThrows(RateLimitExceededException.class, () -> service.processMessage(msg));
    }
}
