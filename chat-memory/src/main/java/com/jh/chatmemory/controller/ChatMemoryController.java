package com.jh.chatmemory.controller;

import com.jh.chatmemory.vo.MemoryReqVo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/memory")
public class ChatMemoryController {

    // Spring 按构造器参数名匹配 Bean 名（chatClient / dashChatClient / redisChatClient）
    private final ChatClient chatClient;
    private final ChatClient dashChatClient;
    private final ChatClient redisChatClient;

    /**
     * 纯内存记忆
     */
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody MemoryReqVo reqVo) {
        return stream(chatClient, reqVo);
    }

    /**
     * JDBC 持久化记忆
     */
    @PostMapping("/chat2")
    public SseEmitter chat2(@RequestBody MemoryReqVo reqVo) {
        return stream(dashChatClient, reqVo);
    }

    /**
     * Redis 持久化记忆
     */
    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody MemoryReqVo reqVo) {
        return stream(redisChatClient, reqVo);
    }

    private SseEmitter stream(ChatClient client, MemoryReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        Flux<String> stream = client
                .prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage()).stream().content();
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
