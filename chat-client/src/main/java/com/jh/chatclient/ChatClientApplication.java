package com.jh.chatclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatClientApplication.class, args);
        System.out.println("=========启动成功=============");
    }

}
