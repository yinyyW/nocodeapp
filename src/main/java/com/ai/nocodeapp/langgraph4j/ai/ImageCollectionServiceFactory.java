package com.ai.nocodeapp.langgraph4j.ai;

import com.ai.nocodeapp.langgraph4j.tools.IllustrationTool;
import com.ai.nocodeapp.langgraph4j.tools.ImageSearchTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ImageCollectionServiceFactory {

    @Resource
    private IllustrationTool illustrationTool;

    @Resource
    private ImageSearchTool imageSearchTool;

    @Resource
    private ChatModel chatModel;

    /**
     * 创建图片收集服务
     */
    @Bean
    public ImageCollectionService createImageCollectionService() {
        return AiServices.builder(ImageCollectionService.class)
                .tools(illustrationTool, imageSearchTool)
                .chatModel(chatModel)
                .build();
    }
}