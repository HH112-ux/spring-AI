package com.jh.quickstart;

import com.jh.quickstart.config.OllamaConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
public class TestOllama {

    private final OllamaConfigProperties config;

    // 构造器注入
    @Autowired
    public TestOllama(OllamaConfigProperties config) {
        this.config = config;
    }

    @Test
    void ollama()  {
        OllamaApi ollamaApi = OllamaApi.builder().build();
        OllamaChatOptions chatOptions = OllamaChatOptions
                .builder()
                .model(config.getModel())
                .disableThinking()
                .build();
        OllamaChatModel chatModel = OllamaChatModel
                .builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(chatOptions)
                .build();
        ChatResponse chatResponse = chatModel.call(new Prompt("who are you"));
        String text = chatResponse.getResult().getOutput().getText();
        System.out.println(text);
    }

    @Test
    void ollamaStream()  {
        OllamaApi ollamaApi = OllamaApi.builder().build();
        OllamaChatOptions chatOptions = OllamaChatOptions
                .builder()
                .model(config.getModel())
                .disableThinking()
                .build();
        OllamaChatModel chatModel = OllamaChatModel
                .builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(chatOptions)
                .build();
        Flux<ChatResponse> stream = chatModel.stream(new Prompt("who are you"));
        stream.toIterable().forEach(System.out::println);


    }
}
