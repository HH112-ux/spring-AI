package com.jh.toolcalling.controller;

import com.jh.toolcalling.tools.OrderTool;
import com.jh.toolcalling.tools.WeatherTool;
import com.jh.toolcalling.tools.WeatherToolV4;
import com.jh.toolcalling.vo.ToolReqVo;
import com.jh.toolcalling.vo.WeatherRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tool")
public class ToolCallingController {

    private final ChatClient chatClient;
    private final FunctionToolCallback<WeatherRequest, String> weatherToolCallback;

    /** V1：@Tool 注解方式 */
    @PostMapping("/chat")
    public SseEmitter chat(@RequestBody ToolReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .tools(new WeatherTool())
                .stream().content()
                .subscribe(chunk -> {
                            try { emitter.send(SseEmitter.event().data(chunk).build()); }
                            catch (Exception e) { emitter.completeWithError(e); }
                        },
                        emitter::completeWithError, emitter::complete);
        return emitter;
    }

    /** V2：订单工具，@Tool + VO 返回复杂对象 */
    @PostMapping("/chat2")
    public SseEmitter chat2(@RequestBody ToolReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .tools(new OrderTool())
                .stream().content()
                .subscribe(chunk -> {
                            try { emitter.send(SseEmitter.event().data(chunk).build()); }
                            catch (Exception e) { emitter.completeWithError(e); }
                        },
                        emitter::completeWithError, emitter::complete);
        return emitter;
    }

    /** V3：FunctionToolCallback Bean 方式 */
    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody ToolReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .toolCallbacks(weatherToolCallback)
                .stream().content()
                .subscribe(chunk -> {
                            try { emitter.send(SseEmitter.event().data(chunk).build()); }
                            catch (Exception e) { emitter.completeWithError(e); }
                        },
                        emitter::completeWithError, emitter::complete);
        return emitter;
    }

    /** V4：ToolCallback 接口 + toolContext 传递 userId */
    @PostMapping("/chat4")
    public SseEmitter chat4(@RequestBody ToolReqVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        chatClient.prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .toolContext(Map.of("userId", reqVo.getConversationId()))
                .toolCallbacks(new WeatherToolV4())
                .stream().content()
                .subscribe(chunk -> {
                            try { emitter.send(SseEmitter.event().data(chunk).build()); }
                            catch (Exception e) { emitter.completeWithError(e); }
                        },
                        emitter::completeWithError, emitter::complete);
        return emitter;
    }
}
