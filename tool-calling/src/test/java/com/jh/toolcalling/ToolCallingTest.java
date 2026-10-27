package com.jh.toolcalling;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class ToolCallingTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    void test() {
        Boolean entity = chatClient.prompt()
                .user("明天会下雨吗").call().entity(Boolean.class);
        System.out.println(entity);
    }

}

