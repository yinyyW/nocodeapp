package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

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
}
