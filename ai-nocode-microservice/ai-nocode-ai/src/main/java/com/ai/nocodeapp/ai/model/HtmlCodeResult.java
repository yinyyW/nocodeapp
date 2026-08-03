package com.ai.nocodeapp.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成的HTML代码文件结果")
@Data
public class HtmlCodeResult {

    /**
     * html文件代码
     */
    @Description("生成的HTML代码")
    String htmlCode;

    /**
     * 文件生成描述
     */
    @Description("生成的HTML代码的描述，使用与用户输入相同的自然语言")
    String description;
}
