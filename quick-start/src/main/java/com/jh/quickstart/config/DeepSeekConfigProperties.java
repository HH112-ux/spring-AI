package com.jh.quickstart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 从 application.yml 读取 Spring AI DeepSeek 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.deepseek")
public class DeepSeekConfigProperties {

    // 读取 spring.ai.deepseek.api-key
    private String apiKey;

    // 读取 spring.ai.deepseek.base-url
    private String baseUrl;

    // 读取 spring.ai.deepseek.chat.options.model
    private String model;

    // 读取 spring.ai.deepseek.chat.options.temperature
    private Double temperature;

    // 读取 spring.ai.deepseek.chat.options.max-tokens
    private Integer maxTokens;

}
