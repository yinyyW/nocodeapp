package com.ai.nocodeapp.core.parser;

/**
 * 代码解析策略接口
 */
public interface CodeParser<T> {

    /**
     * 解析ai生成回答中的代码部分
     * @param content 原始回答内容
     * @return 代码对象
     */
    T parseCode(String content);
}
