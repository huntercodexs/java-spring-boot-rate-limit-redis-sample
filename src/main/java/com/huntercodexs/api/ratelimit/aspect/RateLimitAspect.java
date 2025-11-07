package com.huntercodexs.api.ratelimit.aspect;

import com.huntercodexs.api.ratelimit.annotation.RateLimit;
import com.huntercodexs.api.ratelimit.handler.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// **Usar Aspect**
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Long> redisTemplate;

    // Ponto de corte: interceptar qualquer método anotado com @RateLimit
    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

        // 1. Obter os dados da requisição
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        String ipAddress = request.getRemoteAddr();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 2. Construir a chave do Redis
        // Formato: ratelimit:<IP_CLIENTE>:<NOME_METODO>
        String redisKey = String.format("ratelimit:%s:%s", ipAddress, method.getName());

        // 3. Obter os parâmetros do @RateLimit
        int limit = rateLimit.limit();
        int duration = rateLimit.duration();
        TimeUnit unit = rateLimit.unit();

        // Converter a duração para segundos para o TTL do Redis
        long durationInSeconds = TimeUnit.SECONDS.convert(duration, unit);

        // 4. Lógica de Rate Limiting (Contador Simples)

        // Incrementa o contador da chave. INCR é atômico.
        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        if (currentCount == null) {
            // Em caso de erro na conexão, permite a requisição, mas um código robusto trataria
            // isso de forma mais segura (por exemplo, falhar aberto ou falhar fechado).
            return joinPoint.proceed();
        }

        // Se for o primeiro acesso, define o TTL (Time to Live)
        if (currentCount == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(durationInSeconds));
        }

        // 5. Verificar o limite
        if (currentCount > limit) {
            // Lança a exceção que será capturada pelo GlobalExceptionHandler (429)
            throw new RateLimitExceededException(
                    String.format("Você excedeu o limite de %d requisições por %d %s.",
                            limit, duration, unit.toString().toLowerCase()));
        }

        // 6. Se permitido, prossegue com a execução do método original
        return joinPoint.proceed();
    }
}