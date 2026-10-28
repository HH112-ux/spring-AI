package com.jh.chatrag.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final DashScopeChatModel dashScopeChatModel;

    /**
     * 配置一个带记忆能力的chatClient
     */
    @Bean
    public ChatClient chatClient() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        MessageChatMemoryAdvisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }

}
