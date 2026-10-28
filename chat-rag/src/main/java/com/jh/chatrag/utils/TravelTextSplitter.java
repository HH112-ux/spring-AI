package com.jh.chatrag.utils;

import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.List;

public class TravelTextSplitter extends TextSplitter {

    @Override
    protected List<String> splitText(String text) {
        return List.of(split(text));
    }

    public String[] split(String text) {
        // 按照段落分割：两个或更多换行符为分割点
        return text.split("，");
    }


}
