package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(Duration.ofMinutes(10))
            .expireAfterWrite(Duration.ofMinutes(30))
            .removalListener((key, value, cause) -> {
                log.debug("AI服务实例已删除： {}, 原因： {}", key,  cause);
            })
            .build();

    /**
     * 默认提供一个 Bean
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }

    /**
     * 生成AI服务实例
     * @param appId 应用id
     * @return AI应用
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        log.info("创建AI服务实例，appId={}", appId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .maxMessages(20)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatMemory(chatMemory)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }

    /**
     * 根据appId创建AI服务
     * @param appId 应用id
     * @return AI service
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }
}
