package com.huntercodexs.api.ratelimit.bus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.huntercodexs.api.ratelimit.service.MessageProcessorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "servicebus.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceBusConsumerConfig {

    @Value("${azure.servicebus.queue-name:test}")
    private String queueName;

    @Value("${azure.servicebus.connection-string}")
    private String connectionString;

    @Value("${azure.servicebus.receive-mode:PEEK_LOCK}")
    private String receiveMode;

    private static final Logger log = LoggerFactory.getLogger(ServiceBusConsumerConfig.class);

    private final MessageProcessorService messageProcessorService;

    private ServiceBusProcessorClient client;

    @PostConstruct
    public void startProcess() {
        client = this.serviceBusProcessorClient();
        client.start();
        log.info("Starting Service Bus Processor for queue: {}", queueName);
    }

    @PostConstruct
    public void stopProcess() {
        if (client != null) {
            client.close();
        }
        log.info("Stopping Service Bus Processor for queue: {}", queueName);
    }

    public ServiceBusProcessorClient serviceBusProcessorClient() {
        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .processor()
                .queueName(queueName)
                .prefetchCount(1)
                .maxConcurrentCalls(1)
                .processMessage(context -> {
                    ServiceBusReceivedMessage message = context.getMessage();
                    messageProcessorService.processMessage(message);
                })
                .processError(context -> {
                    log.error("Error occurred while processing message: {}", context.getException().getMessage());
                })
                .receiveMode(ServiceBusReceiveMode.valueOf(receiveMode))
                .disableAutoComplete()
                .maxAutoLockRenewDuration(Duration.ofMinutes(5))
                .buildProcessorClient();
    }
}

