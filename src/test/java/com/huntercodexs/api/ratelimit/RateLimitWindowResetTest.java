package com.huntercodexs.api.ratelimit;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.huntercodexs.api.ratelimit.aspect.RateLimitServiceBusAspect;
import com.huntercodexs.api.ratelimit.exception.RateLimitExceededException;
import com.huntercodexs.api.ratelimit.service.MessageProcessorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

public class RateLimitWindowResetTest {

    @Test
    public void testRateLimitWindowResets() throws Exception {

        MessageProcessorService target = new MessageProcessorService();
        RateLimitServiceBusAspect aspect = new RateLimitServiceBusAspect();

        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);

        MessageProcessorService proxy = factory.getProxy();

        ServiceBusReceivedMessage msg = Mockito.mock(ServiceBusReceivedMessage.class);

        for (int i = 0; i < 5; i++) {
            proxy.processMessage(msg);
        }

        Assertions.assertThrows(RateLimitExceededException.class, () -> proxy.processMessage(msg));

        // wait for more than 1 second to allow the rate limit window to reset
        Thread.sleep(1200);

        for (int i = 0; i < 5; i++) {
            Assertions.assertDoesNotThrow(() -> proxy.processMessage(msg));
        }
    }
}
