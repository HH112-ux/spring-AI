package com.jh.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@AllArgsConstructor
@RequestMapping("/client")
public class ChartClientController {

    private final DashScopeChatModel dashScopeChatModel;

    @PostMapping("/chat")
    public SseEmitter chatStreamPost(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> stream = chatClient.prompt().user(message).stream().content();
        stream.subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }
}
