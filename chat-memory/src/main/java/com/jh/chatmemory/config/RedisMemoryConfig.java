package com.jh.chatmemory.config;

import com.alibaba.cloud.ai.memory.redis.BaseRedisChatMemoryRepository;
import com.alibaba.cloud.ai.memory.redis.JedisRedisChatMemoryRepository;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Data
@Configuration
@ConfigurationProperties(value = "spring.ai.memory.redis")
public class RedisMemoryConfig {

    private String host;
    private Integer port;
    private String username;
    private String password;

    @Bean
    @Primary
    public BaseRedisChatMemoryRepository redisChatMemoryRepository() {
        return JedisRedisChatMemoryRepository
                .builder()
                .host(host)
                .port(port)
                .build();
    }
}
