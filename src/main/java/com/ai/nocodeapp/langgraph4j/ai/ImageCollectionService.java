package com.ai.nocodeapp.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;

import java.util.List;

/**
 * 图片收集服务接口
 */
public interface ImageCollectionService {

    /**
     * 根据用户提示词收集图片
     * @param userPrompt 用户提示词
     * @return 图片列表JSON
     */
    @SystemMessage(fromResource = "prompt/image-collection-system-prompt.md")
    String searchIllustrations(String userPrompt);
}