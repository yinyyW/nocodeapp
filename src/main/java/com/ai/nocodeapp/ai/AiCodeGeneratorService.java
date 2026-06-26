package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.ai.model.HtmlCodeResult;
import com.ai.nocodeapp.ai.model.MultiFileCodeResult;

public interface AiCodeGeneratorService {

    /**
     * 生成单html网页代码
     * @param userMessage 用户提示词
     * @return html代码
     */
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成html, css, javascript代码
     * @param userMessage 用户提示词
     * @return html, css, javascript代码
     */
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
