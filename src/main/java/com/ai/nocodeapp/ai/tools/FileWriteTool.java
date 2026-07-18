package com.ai.nocodeapp.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import com.ai.nocodeapp.constants.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * AI服务文件写入工具
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool {

    @Tool
    public String writeFile(@ToolMemoryId long appId,
                            @P("文件的相对路径") String relativePath,
                            @P("文件的内容") String content) {
        log.error("write file tool: appid {}", appId);
        try {
            // 获取目标目录
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                // 拼接绝对路径
                String projectName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectName);
                path = projectRoot.resolve(relativePath);
            }
            // 创建父目录
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            // 写入文件
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", relativePath);
            return "文件写入成功：" + relativePath;
        } catch (Exception e) {
            String errorMessage =
                    "文件写入失败： " + relativePath + "， 错误： " + e.getMessage();
            log.error(errorMessage);
            return errorMessage;
        }
    }

    @Override
    public String getToolName() {
        return "writeFile";
    }

    @Override
    public String getDisplayName() {
        return "写入文件";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                        [工具调用] 写入文件 %s
                        ```%s
                        %s
                        ```
                        """, relativeFilePath, suffix, content);
    }
}
