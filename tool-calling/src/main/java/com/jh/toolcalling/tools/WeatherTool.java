package com.jh.toolcalling.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class WeatherTool {

    @Tool(description = "根据城市查询天气")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return city + "的天气是晴天";
    }
}
