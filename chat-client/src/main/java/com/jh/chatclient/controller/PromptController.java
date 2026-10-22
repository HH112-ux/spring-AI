package com.jh.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    private final DashScopeChatModel dashScopeChatModel;
    private final Resource systemPrompt;

    public PromptController(
            DashScopeChatModel dashScopeChatModel,
            @Value("classpath:files/prompt.st") Resource systemPrompt
    ) {
        this.dashScopeChatModel = dashScopeChatModel;
        this.systemPrompt = systemPrompt;
    }

    /**
     * 使用 ChatClient 的简洁 API（推荐）
     */
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> stream = chatClient.prompt().system("你是一个法律助手，只回答法律问题，"
                + "其它问题回复，我只能回答法律相关问题，其它无可奉告").user(message).stream().content();
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

    /**
     * 使用底层 Message 对象（更底层灵活）
     */
    @PostMapping("/chat2")
    public SseEmitter chat2(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        SystemMessage systemMessage = new SystemMessage("你是一个讲故事的助手,每个故事控制在300字以内");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(userMessage, systemMessage);
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> stream = chatClient.prompt(prompt).stream().content();
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

    /**
     * 动态提示词
     */
    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        PromptTemplate promptTemplate =
                new PromptTemplate("你是一个{role}，讲一个关于{topic}的故事，200字以内");
        Prompt prompt = promptTemplate.create(Map.of("role", "讲故事的助手",
                "topic", message));
        ChatClient chatClient = ChatClient.builder(dashScopeChatModel).build();
        Flux<String> stream = chatClient.prompt(prompt).stream().content();
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

    /**
     * Lambda表达式提示词
     */
    @PostMapping("/chat4")
    public SseEmitter chat4(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        ChatClient chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultSystem("你是一个{role}，讲一个关于{topic}的故事，100字以内")
                .build();

        Flux<String> stream = chatClient.prompt()
                .system(p->p.param("role", "讲故事助手"))
                .user(message)
                .stream().content();
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

    /**
     * 外部文件读取提示词
     */
    @PostMapping("/chat5")
    public SseEmitter chat5(@RequestBody String message) {
        SseEmitter emitter = new SseEmitter();
        ChatClient chatClient = ChatClient
                .builder(dashScopeChatModel)
                .defaultSystem(systemPrompt)
                .build();

        Flux<String> stream = chatClient.prompt()
                .user(message)
                .stream().content();
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
