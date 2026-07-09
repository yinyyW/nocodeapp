package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 生成单html网页代码
     * @param userMessage 用户提示词
     * @return html代码
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.md")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成html, css, javascript代码
     * @param userMessage 用户提示词
     * @return html, css, javascript代码
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.md")
    MultiFileCodeResult generateMultiFileCode(String userMessage);

    /**
     * 生成单html网页代码（流式）
     * @param userMessage 用户提示词
     * @return 生成过程中的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.md")
    Flux<String> generateHtmlCodeStream(String userMessage);

    /**
     * 生成html, css, javascript代码（流式）
     * @param userMessage 用户提示词
     * @return 生成过程中的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.md")
    Flux<String> generateMultiFileCodeStream(String userMessage);

    /**
     * 生成 vue 项目代码（流式）
     * @param userMessage 用户提示词
     * @return 生成过程中的流式响应
     */
    @SystemMessage(fromResource = "prompt/codegen-vue-project-system-prompt.md")
    TokenStream generateVueProjectCodeStream(@MemoryId long appId, @UserMessage String userMessage);
}
