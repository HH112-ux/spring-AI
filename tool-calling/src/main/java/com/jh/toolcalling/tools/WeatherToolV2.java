package com.jh.toolcalling.tools;

import com.jh.toolcalling.vo.WeatherRequest;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class WeatherToolV2 {

    @Bean
    public FunctionToolCallback<WeatherRequest, String> weatherToolCallback() {
        return FunctionToolCallback.<WeatherRequest, String>builder(
                        "getWeather",
                        request -> request.getCityName() + "的天气是晴天"
                )
                .description("根据城市查询天气信息")
                .inputType(WeatherRequest.class)
                .build();
    }
}
