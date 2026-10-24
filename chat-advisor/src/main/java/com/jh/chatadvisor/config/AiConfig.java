package com.jh.chatadvisor.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.jh.chatadvisor.advisor.LoggingAdvisor;
import com.jh.chatadvisor.advisor.TokenGuardAdvisor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final DashScopeChatModel dashScopeChatModel;
    private final LoggingAdvisor loggingAdvisor;
    private final TokenGuardAdvisor tokenGuardAdvisor;

    @Bean
    public ChatClient chatClient() {
        SafeGuardAdvisor safeGuardAdvisor = new SafeGuardAdvisor(List.of("猫"));
        return ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(loggingAdvisor, tokenGuardAdvisor, safeGuardAdvisor)
                .build();
    }
}
