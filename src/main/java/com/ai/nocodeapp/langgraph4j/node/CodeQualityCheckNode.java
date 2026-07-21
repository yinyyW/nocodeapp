package com.ai.nocodeapp.langgraph4j.node;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.langgraph4j.ai.CodeQualityCheckService;
import com.ai.nocodeapp.langgraph4j.model.QualityResult;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.utils.SpringContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeQualityCheckNode {

    /**
     * 需要检查的文件扩展名
     */
    private static final List<String> CODE_EXTENSIONS = Arrays.asList(
            ".html", ".htm", ".css", ".js", ".json", ".vue", ".ts", ".jsx", ".tsx"
    );


    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("代码校验节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String codeDir = context.getGeneratedCodeDir();
            QualityResult qualityResult;
            try {
                // 拼接文件代码内容
                String codeContent = readAndConcatenateCodeFiles(codeDir);
                if (StrUtil.isBlank(codeContent)) {
                    log.warn("未找到可检查的代码文件");
                    qualityResult = QualityResult.builder()
                            .isValid(false)
                            .errors(List.of("未找到可检查的代码文件"))
                            .suggestions(List.of("请确保代码生成成功"))
                            .build();
                } else {
                    // AI 代码质量检查服务
                    CodeQualityCheckService codeQualityCheckService = SpringContextUtil.getBean(CodeQualityCheckService.class);
                    qualityResult = codeQualityCheckService.checkCodeQuality(codeContent);
                }

            } catch (Exception e) {
                log.error("代码质量检查异常: {}", e.getMessage(), e);
                qualityResult = QualityResult.builder()
                        .isValid(true) // 异常直接跳到下一个步骤
                        .build();
            }
            context.setCurrentStep("代码质量检查");
            context.setQualityResult(qualityResult);
            return WorkflowContext.saveContext(context);
        });
    }

    /**
     * 读取并拼接代码目录下的所有代码文件
     */
    private static String readAndConcatenateCodeFiles(String codeDir) {
        // 获取文件目录
        File dir = new File(codeDir);
        if (!dir.exists() || !dir.isDirectory()) {
            log.error("代码目录不存在或不是目录: {}", codeDir);
            return "";
        }
        // 遍历文件并拼接代码内容
        StringBuilder codeContent = new StringBuilder();
        FileUtil.walkFiles(dir, file -> {
            if (shouldSkipFile(file, dir)) {
                return;
            }
            if (isCodeFile(file)) {
                String relativePath = FileUtil.subPath(codeDir, file);
                codeContent.append("## 文件: ").append(relativePath).append("\n\n");
                String fileContent = FileUtil.readUtf8String(file);
                codeContent.append(fileContent).append("\n\n");
            }
        });
        return codeContent.toString();
    }

    /**
     * 判断是否应该跳过此文件
     */
    private static boolean shouldSkipFile(File file, File rootDir) {
        String relativePath = FileUtil.subPath(rootDir.getAbsolutePath(), file.getAbsolutePath());
        // 跳过隐藏文件
        if (file.getName().startsWith(".")) {
            return true;
        }
        // 跳过特定目录下的文件
        return relativePath.contains("node_modules" + File.separator) ||
                relativePath.contains("dist" + File.separator) ||
                relativePath.contains("target" + File.separator) ||
                relativePath.contains(".git" + File.separator);
    }

    /**
     * 判断是否是需要检查的代码文件
     */
    private static boolean isCodeFile(File file) {
        String fileName = file.getName().toLowerCase();
        return CODE_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }
}
