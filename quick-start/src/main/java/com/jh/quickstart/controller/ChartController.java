package com.jh.quickstart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jh.quickstart.config.DeepSeekConfigProperties;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class ChartController {

    private final DeepSeekConfigProperties config;
    private final ObjectMapper objectMapper;
    private final View error;

    // 构造器注入
    public ChartController(DeepSeekConfigProperties config, ObjectMapper objectMapper, View error) {
        this.config = config;
        this.objectMapper = objectMapper;
        this.error = error;
    }

    @GetMapping("/chat/{message}")
    public ChatResponse chat(@PathVariable String message) {
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        Prompt prompt = new Prompt(message);
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        return chatModel.call(prompt);
    }

    @GetMapping("/chatStream/{message}")
    public SseEmitter chatStream(@PathVariable String message) {
        SseEmitter emitter = new SseEmitter();

        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        Prompt prompt = new Prompt(message);
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Flux<ChatResponse> stream = chatModel.stream(prompt);
        stream.subscribe(chunk -> {
                    try {
                        ChatResponse response = new ChatResponse(chunk.getResults());
                        String json = objectMapper.writeValueAsString(response);

                        emitter.send(SseEmitter.event().data(json).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @PostMapping("/chatStream")
    public SseEmitter chatStreamPost(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();

        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        Prompt prompt = new Prompt(message);
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Flux<ChatResponse> stream = chatModel.stream(prompt);
        stream.subscribe(chunk -> {
                    try {
                        String text = chunk.getResult().getOutput().getText();
                        assert text != null;
                        emitter.send(SseEmitter.event().data(text).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }
}
