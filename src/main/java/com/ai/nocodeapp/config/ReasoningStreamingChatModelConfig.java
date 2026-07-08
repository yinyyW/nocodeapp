package com.ai.nocodeapp.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
public class ReasoningStreamingChatModelConfig {
    String baseUrl;

    String apiKey;

    String modelName;

    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        // 生产环境使用
        // final int maxTokens = 327668;
        // final String modelName = "deepseek-v4-pro";
        // 开发环境使用
        final int maxTokens = 8192;
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
