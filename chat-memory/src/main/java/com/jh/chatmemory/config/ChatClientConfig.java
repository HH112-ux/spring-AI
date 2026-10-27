package com.jh.chatmemory.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.memory.redis.BaseRedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final DashScopeChatModel dashScopeChatModel;
    private final JdbcChatMemoryRepository jdbcChatMemoryRepository;
    private final BaseRedisChatMemoryRepository redisChatMemoryRepository;

    /**
     * 纯内存 ChatMemory（服务重启丢失）
     */
    @Bean
    public ChatClient chatClient() {
        var chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }

    /**
     * JDBC 持久化 ChatMemory
     */
    @Bean
    public ChatClient dashChatClient() {
        var chatMemory = MessageWindowChatMemory
                .builder()
                .maxMessages(10)
                .chatMemoryRepository(jdbcChatMemoryRepository).build();
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }

    /**
     * Redis 持久化 ChatMemory
     */
    @Bean
    public ChatClient redisChatClient() {
        var chatMemory = MessageWindowChatMemory
                .builder()
                .maxMessages(10)
                .chatMemoryRepository(redisChatMemoryRepository).build();
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        return ChatClient.builder(dashScopeChatModel).defaultAdvisors(memoryAdvisor).build();
    }
}
