package com.jh.chatadvisor.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
public class TokenGuardAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(TokenGuardAdvisor.class);

    // 最大允许的 Token 数量，按照您的模型自行调整
    private static final int MAX_TOKENS = 20;


    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        Prompt originalPrompt = chatClientRequest.prompt();
        List<Message> modifiedMessages = new ArrayList<>();
        boolean modified = false;

        for (Message message : originalPrompt.getInstructions()) {
            if (message instanceof UserMessage userMsg) {
                String content = userMsg.getText();

                int estimatedTokens = (int) (content.length() * 0.8);

                if (estimatedTokens > MAX_TOKENS) {
                    logger.warn("输入过长: {} tokens，自动截断至 {}", estimatedTokens, MAX_TOKENS);

                    int truncatedLength = (int) (MAX_TOKENS / 0.8);
                    String truncatedContent = content.substring(0, truncatedLength);


                    UserMessage truncatedMessage = UserMessage.builder()
                            .text(truncatedContent)
                            .media(userMsg.getMedia())
                            .metadata(userMsg.getMetadata())
                            .build();

                    modifiedMessages.add(truncatedMessage);
                    modified = true;
                } else {
                    modifiedMessages.add(message);
                }
            } else {
                modifiedMessages.add(message);
            }
        }

        ChatClientRequest modifiedRequest = chatClientRequest;
        if (modified) {
            Prompt newPrompt = new Prompt(modifiedMessages);
            modifiedRequest = new ChatClientRequest(newPrompt, chatClientRequest.context());
        }

        return callAdvisorChain.nextCall(modifiedRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Prompt originalPrompt = chatClientRequest.prompt();
        List<Message> modifiedMessages = new ArrayList<>();
        boolean modified = false;

        for (Message message : originalPrompt.getInstructions()) {
            if (message instanceof UserMessage userMsg) {
                String content = userMsg.getText();

                int estimatedTokens = (int) (content.length() * 0.8);

                if (estimatedTokens > MAX_TOKENS) {
                    logger.warn("输入过长: {} tokens，自动截断至 {}", estimatedTokens, MAX_TOKENS);

                    int truncatedLength = (int) (MAX_TOKENS / 0.8);
                    logger.warn("content{}", content);
                    String truncatedContent = content.substring(0, truncatedLength);


                    UserMessage truncatedMessage = UserMessage.builder()
                            .text(truncatedContent)
                            .media(userMsg.getMedia())
                            .metadata(userMsg.getMetadata())
                            .build();

                    logger.warn("截断后的文本{}", truncatedContent);
                    modifiedMessages.add(truncatedMessage);
                    modified = true;
                } else {
                    modifiedMessages.add(message);
                }
            } else {
                modifiedMessages.add(message);
            }
        }

        ChatClientRequest modifiedRequest = chatClientRequest;
        if (modified) {
            Prompt newPrompt = new Prompt(modifiedMessages);
            modifiedRequest = new ChatClientRequest(newPrompt, chatClientRequest.context());
        }

        return streamAdvisorChain.nextStream(modifiedRequest);
    }

    @Override
    public String getName() {
        return "token-guard-advisor";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
