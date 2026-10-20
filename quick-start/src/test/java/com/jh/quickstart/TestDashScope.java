package com.jh.quickstart;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioSpeechApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeAudioTranscriptionApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeImageApi;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioSpeechModel;
import com.alibaba.cloud.ai.dashscope.audio.DashScopeAudioTranscriptionModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesis;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisParam;
import com.alibaba.dashscope.aigc.videosynthesis.VideoSynthesisResult;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.jh.quickstart.config.DashScopeConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
public class TestDashScope {

    private final DashScopeConfigProperties config;

    // 构造器注入
    @Autowired
    public TestDashScope(DashScopeConfigProperties config) {
        this.config = config;
    }

    @Test
    void dashScopeTest() {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(config.getApiKey()).build();
        DashScopeChatModel dashScopeChatModel = DashScopeChatModel.builder().dashScopeApi(dashScopeApi).build();
        String content = dashScopeChatModel.call("你是谁？");
        System.out.println(content);
    }

    //文生图
    @Test
    void dashTextToImage() {
        DashScopeImageApi dashScopeImageApi = DashScopeImageApi.builder().apiKey(config.getApiKey()).build();
        DashScopeImageModel imageModel = DashScopeImageModel.builder().dashScopeApi(dashScopeImageApi).build();
        ImagePrompt imagePrompt = new ImagePrompt("天气预报图");
        ImageResponse response = imageModel.call(imagePrompt);
        String url = response.getResult().getOutput().getUrl();
        System.out.println(url);
    }

    //文生语音
    @Test
    void dashTextToVoice() {
        DashScopeAudioSpeechApi dashScopeAudioSpeechApi = new DashScopeAudioSpeechApi(config.getApiKey());
        DashScopeAudioSpeechModel audioSpeechModel = DashScopeAudioSpeechModel.builder().audioSpeechApi(dashScopeAudioSpeechApi).build();
        TextToSpeechResponse response = audioSpeechModel.call(new TextToSpeechPrompt("天气预报播报"));
        File file = new File(System.getProperty("user.dir") + "/output.mp3");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] output = response.getResult().getOutput();
            fos.write(output);
            fos.flush();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    //语音翻译文本 目前来看有bug
    @Test
    void dashVoiceToText() throws Exception {
        DashScopeAudioTranscriptionApi transcriptionApi = DashScopeAudioTranscriptionApi
                .builder()
                .apiKey(config.getApiKey())
                .model(DashScopeModel.AudioModel.PARAFORMER_V2.getValue())
                .build();

        DashScopeAudioTranscriptionModel transcriptionModel = DashScopeAudioTranscriptionModel
                .builder()
                .audioTranscriptionApi(transcriptionApi)
                .build();

        String audioPath = System.getProperty("user.dir") + "/output.mp3";
        FileSystemResource fileResource = new FileSystemResource(audioPath);

        String output = transcriptionModel
                .call(new AudioTranscriptionPrompt(fileResource)).getResult().getOutput();
        System.out.println(output);
    }

    //多模态- 图像识别
    @Test
    void dashMulTest() {
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(config.getApiKey()).build();
        ClassPathResource classPathResource = new ClassPathResource("/file/weather.jpeg");
        Media media = new Media(MimeType.valueOf(MimeTypeUtils.IMAGE_JPEG_VALUE), classPathResource);
        DashScopeChatOptions options = DashScopeChatOptions.builder().multiModel(true).model("qwen3.6-plus").build();
        Prompt prompt = Prompt.builder().chatOptions(options).messages(UserMessage.builder().media(media).text("识别图片中天气").build()).build();
        DashScopeChatModel chatModel = DashScopeChatModel.builder().dashScopeApi(dashScopeApi).build();
        ChatResponse response = chatModel.call(prompt);
        System.out.println(response.getResult().getOutput().getText());
    }

    @Test
    void dashTextToVideo() throws NoApiKeyException, InputRequiredException {
        VideoSynthesis synthesis = new VideoSynthesis();
        VideoSynthesisParam param = VideoSynthesisParam.builder()
                .apiKey(config.getApiKey())
                .model("wan2.6-t2v")
                .prompt("一段紧张刺激的侦探追查故事，展现电影级叙事能力。第1个镜头[0-3秒] 全景：雨夜的纽约街头，霓虹灯闪烁，一位身穿黑色风衣的侦探快步行走。 第2个镜头[3-6秒] 中景：侦探进入一栋老旧建筑，雨水打湿了他的外套，门在他身后缓缓关闭。 第3个镜头[6-9秒] 特写：侦探的眼神坚毅专注，远处传来警笛声，他微微皱眉思考。 第4个镜头[9-12秒] 中景：侦探在昏暗走廊中小心前行，手电筒照亮前方。 第5个镜头[12-15秒] 特写：侦探发现关键线索，脸上露出恍然大悟的表情。")
                .duration(15)
                .size("1280*720")
                .shotType("multi")
                .promptExtend(true)
                .watermark(true)
                .build();
        System.out.println("please wait...");
        VideoSynthesisResult result = synthesis.call(param);
        System.out.println(JsonUtils.toJson(result));
    }


}
