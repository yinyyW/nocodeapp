package com.ai.nocodeapp.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
public class MultiFileCodeResult {

    /**
     * html文件代码
     */
    @Description("生成的HTML代码")
    String htmlCode;

    /**
     * css文件代码
     */
    @Description("生成的CSS代码")
    String cssCode;

    /**
     * javascript文件代码
     */
    @Description("生成的JavaScript代码")
    String jsCode;

    /**
     * 文件生成描述
     */
    @Description("生成的代码描述，使用与用户输入相同的自然语言")
    String description;

}
