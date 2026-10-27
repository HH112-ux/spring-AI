package com.jh.toolcalling.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatClientConfig {

    private final DashScopeChatModel dashScopeChatModel;

    /**
     * 带记忆能力的 ChatClient
     */
    @Bean
    public ChatClient chatClient() {
        var chatMemory = MessageWindowChatMemory.builder().build();
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }
}
