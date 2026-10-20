package com.jh.quickstart;

import com.jh.quickstart.config.DeepSeekConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest
public class TestDeepSeek {

    private final DeepSeekConfigProperties config;

    // 构造器注入
    @Autowired
    public TestDeepSeek(DeepSeekConfigProperties config) {
        this.config = config;
    }

    @Test
    void deepSeekTest(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        String call = chatModel.call("你好，你是谁？");
        System.out.println(call);
    }


    @Test
    void deepSeekTestStream(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).build();
        Flux<String> stream = chatModel.stream("你好，你是谁？");
        stream.toIterable().forEach(System.out::println);
    }


    @Test
    void deepSeekTestOptions(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder().model(DeepSeekApi.DEFAULT_CHAT_MODEL).temperature(1.7).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(chatOptions).build();
        Flux<String> stream = chatModel.stream("你现在是一个演员，形容下你自己");
        stream.toIterable().forEach(System.out::println);
    }

    @Test
    void deepSeekTestOptionsMaxToken(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder().model(DeepSeekApi.DEFAULT_CHAT_MODEL).maxTokens(5).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(chatOptions).build();
        Flux<String> stream = chatModel.stream("你现在是一个演员，形容下你自己");
        stream.toIterable().forEach(System.out::println);
    }


    @Test
    void deepSeekTestOptionsStop(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        List<String> stopWords = new ArrayList<>();
        stopWords.add("我");
        stopWords.add("减肥");
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder().model(DeepSeekApi.DEFAULT_CHAT_MODEL).stop(stopWords).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(chatOptions).build();
        Flux<String> stream = chatModel.stream("你现在是一个演员，形容下你自己");
        stream.toIterable().forEach(System.out::println);
    }


    @Test
    void deepSeekTestOptionsReason(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder().model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(chatOptions).build();
//       需要一个prompt
        Prompt prompt = new Prompt("你是谁？");
        ChatResponse call = chatModel.call(prompt);
        DeepSeekAssistantMessage output =(DeepSeekAssistantMessage) call.getResult().getOutput();
        System.out.println(output.getReasoningContent());
        System.out.println("-----------------------------------------------");
        System.out.println(output.getText());
    }

    @Test
    void deepSeekTestOptionsReasonStream(){
        DeepSeekApi deepSeekApi = DeepSeekApi.builder()
                .apiKey(config.getApiKey())
                .baseUrl(config.getBaseUrl())
                .build();
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder().model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER).build();
        DeepSeekChatModel chatModel = DeepSeekChatModel.builder().deepSeekApi(deepSeekApi).defaultOptions(chatOptions).build();
//       需要一个prompt
        Prompt prompt = new Prompt("你是谁？");
        Flux<ChatResponse> stream = chatModel.stream(prompt);
        stream.toIterable().forEach((chatResponse)->{
            DeepSeekAssistantMessage output = (DeepSeekAssistantMessage)chatResponse.getResult().getOutput();
            System.out.println(output.getReasoningContent());
            System.out.println(output.getText());
        });
    }
}
