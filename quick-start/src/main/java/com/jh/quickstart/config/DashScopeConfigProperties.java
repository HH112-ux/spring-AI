package com.jh.quickstart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.dashscope")
public class DashScopeConfigProperties {

    // 读取 spring.ai.dashscope.api-key
    private String apiKey;

    private String baseUrl;
}
