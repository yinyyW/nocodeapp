package com.ai.nocodeapp.langgraph4j.node;

import com.ai.nocodeapp.constants.AppConstant;
import com.ai.nocodeapp.core.AiCodeGeneratorFacade;
import com.ai.nocodeapp.langgraph4j.model.QualityResult;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class CodeGeneratorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("代码生成节点");
            WorkflowContext context = WorkflowContext.getContext(state);

            // 获取AI代码生成服务
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            String prompt = buildPrompt(context);
            CodeGenTypeEnum codeGenTypeEnum = context.getGenerationType();

            // 代码生成
            Long appId = 0L;
            Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(appId, prompt, codeGenTypeEnum);
            codeStream.blockLast(Duration.ofMinutes(10));
            String projectRootDir = String.format("%s/%s_%s", AppConstant.CODE_OUTPUT_ROOT_DIR, context.getGenerationType(), appId);

            // 更新状态
            context.setCurrentStep("代码生成");
            context.setGeneratedCodeDir(projectRootDir);
            return WorkflowContext.saveContext(context);
        });
    }

    private static String buildPrompt(WorkflowContext context) {
        String message = context.getEnhancedPrompt();
        QualityResult qualityResult = context.getQualityResult();
        if (isQualityCheckFailed(qualityResult)) {
            message = buildErrorFixPrompt(qualityResult);
        }
        return message;
    }

    /**
     * 判断质检是否失败
     */
    private static boolean isQualityCheckFailed(QualityResult qualityResult) {
        return qualityResult != null &&
                !qualityResult.getIsValid() &&
                qualityResult.getErrors() != null &&
                !qualityResult.getErrors().isEmpty();
    }

    /**
     * 构造错误修复提示词
     */
    private static String buildErrorFixPrompt(QualityResult qualityResult) {
        StringBuilder errorInfo = new StringBuilder();
        errorInfo.append("\n\n## 上次生成的代码存在以下问题，请修复：\n");
        // 添加错误列表
        qualityResult.getErrors().forEach(error ->
                errorInfo.append("- ").append(error).append("\n"));
        // 添加修复建议（如果有）
        if (qualityResult.getSuggestions() != null && !qualityResult.getSuggestions().isEmpty()) {
            errorInfo.append("\n## 修复建议：\n");
            qualityResult.getSuggestions().forEach(suggestion ->
                    errorInfo.append("- ").append(suggestion).append("\n"));
        }
        errorInfo.append("\n请根据上述问题和建议重新生成代码，确保修复所有提到的问题。");
        return errorInfo.toString();
    }
}
