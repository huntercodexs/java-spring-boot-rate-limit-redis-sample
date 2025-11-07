package com.huntercodexs.api.ratelimit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// Configuração do Redis
@Configuration
public class RedisConfig {

    // Configuração de host e porta (podem vir do application.properties)
    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    // A senha do cliente deve vir de uma fonte segura
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * Configuração da conexão com o Redis, incluindo autenticação.
     * **Usar Redis com autenticacao de cliente (via configuracao direta em codigo)**
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        // Autenticação (senha do cliente)
        config.setPassword(redisPassword);

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Long> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Long> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer para as chaves
        template.setKeySerializer(new StringRedisSerializer());

        // Use Long serializer para os valores (contagem de requisições)
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}

