package com.jh.chatadvisor.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/advisor")
public class AdvisorController {

    private final ChatClient chatClient;

    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        chatClient
                .prompt()
                .system("你是一个法律助手，只回答法律问题，"
                        + "其它问题回复，我只能回答法律相关问题，其它无可奉告);")
                .user(message)
                .stream()
                .content()
                .subscribe(chunk -> {
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
