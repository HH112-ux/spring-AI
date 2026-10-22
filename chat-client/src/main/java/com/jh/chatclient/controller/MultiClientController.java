package com.jh.chatclient.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.jh.chatclient.vo.MultiChatReqVo;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.HashMap;

@RestController
@RequestMapping("/multi")
public class MultiClientController {

    HashMap<String, ChatModel> models = new HashMap<>();

    public MultiClientController(
            DashScopeChatModel dashScopeChatModel,
            DeepSeekChatModel deepSeekChatModel,
            OllamaChatModel ollamaChatModel
    ) {
        models.put("dashScope", dashScopeChatModel);
        models.put("deepSeek", deepSeekChatModel);
        models.put("ollama", ollamaChatModel);
    }

    /**
     * 多模型流式对话
     *
     * @param reqVo 消息内容
     */
    @PostMapping("/chat")
    public SseEmitter chatStreamPost(@RequestBody MultiChatReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        ChatModel chatModel = models.get(reqVo.getPlatform());
        ChatClient client = ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().model(reqVo.getModel()).build())
                .build();
        Flux<String> content = client.prompt(reqVo.getMessage()).stream().content();
        content.subscribe(chunk -> {
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
