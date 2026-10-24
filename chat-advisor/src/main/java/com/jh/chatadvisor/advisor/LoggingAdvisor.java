package com.jh.chatadvisor.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class LoggingAdvisor implements CallAdvisor, StreamAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAdvisor.class);

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        String model = chatClientRequest.prompt().getOptions() != null
                ? chatClientRequest.prompt().getOptions().getModel()
                : "unknown";
        logger.info("【AI 请求】模型: {}, Prompt: {}", model, chatClientRequest.prompt().getContents());

        try {
            ChatClientResponse response = callAdvisorChain.nextCall(chatClientRequest);
            ChatResponse chatResponse = response.chatResponse();
            assert chatResponse != null;
            String content = chatResponse.getResult().getOutput().getText();

            if (content == null) {
                content = "";
            }
            logger.info("【AI 响应】Token 消耗: {}, 内容: {}",
                    chatResponse.getMetadata().getUsage().getTotalTokens(),
                    content);
            return response;
        } catch (Exception e) {
            logger.error("【AI调用异常】请求处理失败，提示词：{}，异常信息：{}",
                    chatClientRequest.prompt(), e.getMessage(), e);
            throw e;
        }

    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        logger.info("【AI流式调用请求】提示词内容：{}", chatClientRequest.prompt());

        Flux<ChatClientResponse> responseFlux = streamAdvisorChain.nextStream(chatClientRequest);

        return responseFlux
                .doOnNext(response -> {
                    assert response.chatResponse() != null;
                    String content = response.chatResponse().getResult().getOutput().getText();
                    logger.info("【AI流式响应片段】内容：{}", content);
                })
                .doOnComplete(() -> {
                    logger.info("【AI流式调用完成】提示词：{}", chatClientRequest.prompt());
                })
                .doOnError(e -> {
                    logger.error("【AI流式调用异常】提示词：{}，异常：{}",
                            chatClientRequest.prompt(), e.getMessage(), e);
                });

    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
