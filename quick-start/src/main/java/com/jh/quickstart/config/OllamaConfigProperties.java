package com.jh.quickstart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 从 application.yml 读取 Spring AI ollama 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.ollama")
public class OllamaConfigProperties {

    // 读取 spring.ai.ollama.api-key
    // private String apiKey;

    // 读取 spring.ai.ollama.base-url
    private String baseUrl;

    // 读取 spring.ai.ollama.chat.options.model
    private String model;

}
