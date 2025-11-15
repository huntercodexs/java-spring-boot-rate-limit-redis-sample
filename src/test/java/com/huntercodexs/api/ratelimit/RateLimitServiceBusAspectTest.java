package com.huntercodexs.api.ratelimit;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.huntercodexs.api.ratelimit.aspect.RateLimitServiceBusAspect;
import com.huntercodexs.api.ratelimit.exception.RateLimitExceededException;
import com.huntercodexs.api.ratelimit.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public class RateLimitServiceBusAspectTest {

    @Test
    public void testRateLimitBlocksAfterLimit() {
        // Create a real instance of the service
        MessageProcessorService target = new MessageProcessorService();

        // Create the aspect
        RateLimitServiceBusAspect aspect = new RateLimitServiceBusAspect();

        // Create proxy with aspect applied
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);

        MessageProcessorService proxy = factory.getProxy();

        // Mock the ServiceBusReceivedMessage message
        ServiceBusReceivedMessage msg = Mockito.mock(ServiceBusReceivedMessage.class);

        // Send 5 messages within the limit set by the annotation (5 per 1 second)
        for (int i = 0; i < 5; i++) {
            Assertions.assertDoesNotThrow(() -> proxy.processMessage(msg));
        }

        // Test that the 6th message exceeds the rate limit
        Assertions.assertThrows(RateLimitExceededException.class, () -> proxy.processMessage(msg));
    }
}
