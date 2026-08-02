package com.ai.nocodeapp.model.dto;

import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import lombok.Data;

/**
 * AI代码生成类型路由结果
 * 用于匹配LangChain4j在strict-json-schema/json_object模式下生成的JSON结构
 */
@Data
public class CodeGenTypeRoutingResult {
    private CodeGenTypeEnum codeGenerationType;
}
