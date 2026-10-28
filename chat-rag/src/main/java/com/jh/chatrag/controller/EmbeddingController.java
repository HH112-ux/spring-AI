package com.jh.chatrag.controller;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.jh.chatrag.service.EmbeddingService;
import com.jh.chatrag.utils.TravelTextSplitter;
import com.jh.chatrag.vo.EmbeddingVo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/embedding")
public class EmbeddingController {

    private final DashScopeEmbeddingModel dashScopeEmbeddingModel;

   // private final ZhiPuAiEmbeddingModel zhipuAiEmbeddingModel;

    private final EmbeddingService embeddingService;

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    @PostMapping("/chat")
    public void chat(@RequestBody EmbeddingVo reqVo) {
        EmbeddingResponse embeddingResponse = dashScopeEmbeddingModel.embedForResponse(List.of(reqVo.getMessage()));
        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));
    }

//    @PostMapping("/chat2")
//    public void chat2(@RequestBody EmbeddingVo reqVo) {
//        EmbeddingResponse embeddingResponse = zhipuAiEmbeddingModel.embedForResponse(List.of(reqVo.getMessage()));
//        System.out.println(Arrays.toString(embeddingResponse.getResult().getOutput()));
//    }


    /**
     * 相似文本查找接口（支持指定算法）
     * @param query 查询文本
     * @param algorithm 相似度算法（默认 COSINE）
     */
    @GetMapping("/similarity")
    public Map<String, Object> findSimilarText(
            @RequestParam("query") String query,
            @RequestParam(value = "algorithm", defaultValue = "COSINE") String algorithm) {
        // 校验算法参数合法性
        EmbeddingService.SimilarityAlgorithm simAlgo;
        try {
            simAlgo = EmbeddingService.SimilarityAlgorithm.valueOf(algorithm.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Map.of(
                    "error", "算法参数无效，支持的算法：COSINE/EUCLIDEAN/MANHATTAN",
                    "code", 400
            );
        }

        // 执行相似文本查找
        String similarText = embeddingService.queryBestMatch(query, simAlgo);
        return Map.of(
                "query", query,
                "algorithm", simAlgo.name(),
                "answer", similarText
        );
    }


    @GetMapping("/store")
    public void store() {
        Resource resource = new ClassPathResource("/knowledge/travel.text");
        TextReader textReader = new TextReader(resource);
        List<Document> documents = textReader.get();
        documents.forEach(System.out::println);
        TextSplitter textSplitter = new TravelTextSplitter();
        List<Document> documentList = textSplitter.apply(documents);
        documentList.forEach(System.out::println);
        vectorStore.add(documentList);
    }

    @GetMapping("/search")
    public  List<Document> search(@RequestParam("query") String query) {
        SearchRequest searchRequest = SearchRequest
                .builder()
                .query(query)
                .topK(5)
                .similarityThreshold(0.6)// 相似度阈值
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    @PostMapping("/chat3")
    public SseEmitter chat3(@RequestBody EmbeddingVo reqVo) {
        SseEmitter emitter = new SseEmitter();

        List<Document> searchResult = search(reqVo.getMessage());
        // 2. 构造提示词模板：组织原问题和检索结果
        PromptTemplate promptTemplate = new PromptTemplate(
                "{question}\n\n 用以下信息回答问题:\n {contents}"
        );

        // 3. 填充模板：将真实值代入占位符
        Prompt prompt = promptTemplate.create(Map.of(
                "question", reqVo.getMessage(),
                "contents", searchResult
        ));

        Flux<String> stream = chatClient
                .prompt(prompt)
                .advisors(advisor->advisor.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .stream().content();
        stream.subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }

    @PostMapping("/chat4")
    public SseEmitter chat4(@RequestBody EmbeddingVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        // 1. 创建文档检索器
        VectorStoreDocumentRetriever documentRetriever =
                VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .topK(3)
                        .build();

        // 2. 创建 RAG Advisor
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(documentRetriever)
                        .build();

        // 3. 使用 advisor 增强提示
        Flux<String> stream = chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.advisors(retrievalAugmentationAdvisor).param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .stream()
                .content();
        stream.subscribe(chunk -> {
                    try {
                        emitter.send(SseEmitter.event().data(chunk).build());
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError, emitter::complete);
        return emitter;
    }


    @jakarta.annotation.Resource
    private ChatClient.Builder chatClientBuilder;

    @PostMapping("/chat5")
    public SseEmitter chat5(@RequestBody EmbeddingVo reqVo) {
        SseEmitter emitter = new SseEmitter();
        // 1. 查询压缩：结合聊天历史，压缩冗余信息
        CompressionQueryTransformer queryTransformer =
                CompressionQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .build();

        // 2. 查询扩展：生成多个问题视角
        MultiQueryExpander queryExpander =
                MultiQueryExpander.builder()
                        .chatClientBuilder(chatClientBuilder)
                        .build();

        // 3. 文档检索：向量相似度搜索
        DocumentRetriever documentRetriever =
                VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(3)
                        .build();

        // 4. 文档合并：连接所有检索结果
        DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();

        // 5. 查询增强：基于检索文档优化查询
        QueryAugmenter queryAugmenter =
                ContextualQueryAugmenter.builder().build();

        // 构建完整的 RAG Advisor
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor =
                RetrievalAugmentationAdvisor.builder()
                        .queryTransformers(queryTransformer)
                        .queryExpander(queryExpander)
                        .documentRetriever(documentRetriever)
                        .documentJoiner(documentJoiner)
                        .queryAugmenter(queryAugmenter)
                        .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder().maxMessages(10).build();
        // 结合聊天记忆 Advisor
        Flux<String> stream = chatClient.prompt()
                .advisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        retrievalAugmentationAdvisor
                )
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, reqVo.getConversationId()))
                .user(reqVo.getMessage())
                .stream()
                .content();

        stream.subscribe(chunk -> {
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
