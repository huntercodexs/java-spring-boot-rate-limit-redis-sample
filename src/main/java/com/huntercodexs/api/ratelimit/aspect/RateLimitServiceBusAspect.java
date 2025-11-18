package com.huntercodexs.api.ratelimit.aspect;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huntercodexs.api.ratelimit.annotation.RateLimitServiceBus;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@SuppressWarnings({"java:S3776", "java:S3457"})
public class RateLimitServiceBusAspect {

    @Value("${rate-limit-service-bus.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit-service-bus.limit:0}")
    private int customLimit;

    @Value("${rate-limit-service-bus.duration:0}")
    private int customDuration;

    @Value("${rate-limit-service-bus.unit:seconds}")
    private String customUnit;

    @Value("${rate-limit-service-bus.cache-prefix:rateLimitServiceBusDefaultKeyName}")
    private String customPrefix;

    @Value("${rate-limit-service-bus.key-parameter:_MENSAGEM_INTEIRA_}")
    private String customerKeyParameter;

    private static final Logger log = LoggerFactory.getLogger(RateLimitServiceBusAspect.class);

    private static final String MSG_RATE_LIMIT_EXCEEDED = "Limit of %d requests exceeded for key '%s' in %d %s.";

    private final RedisTemplate<String, Long> redisTemplate;

    private final ObjectMapper mapper = new ObjectMapper();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new StandardReflectionParameterNameDiscoverer();

    @Around("@annotation(rateLimitServiceBus)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimitServiceBus rateLimitServiceBus) throws Throwable {

        if (!rateLimitEnabled) {
            log.warn("The service bus rate limiting is disabled via configuration.");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // Get method arguments (message, headers, etc.)

        // Getting Rate Limit Key Value
        String keyParameterName = rateLimitServiceBus.keyParameterName();
        if (customerKeyParameter != null && !customerKeyParameter.isEmpty()) keyParameterName = customerKeyParameter;

        Object rateLimitKeyValue = findParameterValue(method, args, keyParameterName);

        if (rateLimitKeyValue == null && !keyParameterName.equals("_MENSAGEM_INTEIRA_")) {
            // If the key parameter is not found or is null, handle accordingly.
            log.warn("Alert: Key parameter for Rate Limit not found or is null. Request allowed.");
            return joinPoint.proceed();
        }

        // Building the Redis Key - Format: rateLimitServiceBusKeyName:consumer:<METHOD_NAME>:<KEY_VALUE>
        String redisKey = String.format(customPrefix + ":consumer:%s:%s", method.getName(), keyParameterName);

        // Rate limiting logic
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Prevent null pointer exception, though it shouldn't happen
            return joinPoint.proceed();
        }

        // Get values from annotation
        int limit = rateLimitServiceBus.limit();
        if (customLimit > 0) limit = customLimit;

        int duration = rateLimitServiceBus.duration();
        if (customDuration > 0) duration = customDuration;

        // TTL Setup for the key on first increment
        TimeUnit unit = rateLimitServiceBus.unit();

        if (customUnit.equalsIgnoreCase("SECONDS")) {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase("MINUTES")) {
            if (currentCount == 1) {
                unit = TimeUnit.MINUTES;
                redisTemplate.expire(redisKey, Duration.ofMinutes(TimeUnit.MINUTES.convert(duration, unit)));
            }
        } else if (customUnit.equalsIgnoreCase("HOURS")) {
            if (currentCount == 1) {
                unit = TimeUnit.HOURS;
                redisTemplate.expire(redisKey, Duration.ofHours(TimeUnit.HOURS.convert(duration, unit)));
            }
        } else {
            if (currentCount == 1) {
                unit = TimeUnit.SECONDS;
                redisTemplate.expire(redisKey, Duration.ofSeconds(TimeUnit.SECONDS.convert(duration, unit)));
            }
        }

        log.info("Rate Limit Service Bus Check - Key: {}, Count: {}, Limit: {}/{} {}", redisKey, currentCount, limit, duration, unit);

        if (currentCount > limit) {
            limitExceededAction(args, keyParameterName, limit, duration, unit);
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    private void limitExceededAction(Object[] args, Object keyParameterName, int limit, int duration, TimeUnit unit) {

        String mensagemExcedida = String.format(
                "Rate Limite excedido para a chave %s com limit de %d requisicoes em %d %s.",
                keyParameterName, limit, duration, unit.toString().toLowerCase());

        log.error("429 TOO_MANY_REQUESTS - {}", mensagemExcedida);

        ServiceBusReceivedMessageContext message = (ServiceBusReceivedMessageContext) args[0];

        /*Make some code here to process the message*/

        throw new RateLimitExceededException(String.format(
                MSG_RATE_LIMIT_EXCEEDED, limit, keyParameterName, duration, unit.toString().toLowerCase()));
    }

    private Object findParameterValue(Method method, Object[] args, String parameterName) {
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(parameterName)) {
                    return args[i];
                }
            }
        }
        return null;
    }
}