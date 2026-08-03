package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.model.dto.CodeGenTypeRoutingResult;
import dev.langchain4j.service.SystemMessage;

/**
 * AI代码生成类型智能路由服务
 * 使用结构化输出返回路由结果
 */
public interface AiCodeGenTypeRoutingService {

    /**
     * 根据用户需求智能选择代码生成类型
     *
     * @param userPrompt 用户输入的需求描述
     * @return 推荐的代码生成类型路由结果
     */
    @SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.md")
    CodeGenTypeRoutingResult routeCodeGenType(String userPrompt);
}
