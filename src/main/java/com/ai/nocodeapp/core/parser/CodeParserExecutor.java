package com.ai.nocodeapp.core.parser;

import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;

/**
 * 代码解析器执行器
 */
public class CodeParserExecutor {

    /**
     * HTML单文件生成代码解析器
     */
    private static final HtmlCodeParser htmlCodeParser = new HtmlCodeParser();

    /**
     * 多文件生成代码解析器
     */
    private static final MultiFileCodeParser multiFileCodeParser = new MultiFileCodeParser();

    /**
     * 根据代码生成类型解析文本
     * @param codeGenTypeEnum 代码生成类型
     * @param content 文本
     * @return 解析结果
     */
    public static Object executeParser(CodeGenTypeEnum codeGenTypeEnum, String content) {
        return switch (codeGenTypeEnum) {
            case CodeGenTypeEnum.HTML -> {
                yield htmlCodeParser.parseCode(content);
            }
            case  CodeGenTypeEnum.MULTI_FILE -> {
                yield multiFileCodeParser.parseCode(content);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持对象类型");
        };
    }
}
