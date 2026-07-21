package com.ai.nocodeapp.langgraph4j.node;

import cn.hutool.core.util.StrUtil;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class PromptEnhancerNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行提示词增强节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            String imageStr = context.getImageListStr();
            // 拼接提示词
            StringBuilder enhancedPromptBuilder = new StringBuilder();
            enhancedPromptBuilder.append(originalPrompt);
            if (!StrUtil.isBlank(imageStr)) {
                enhancedPromptBuilder.append("\n\n## 可用素材资源和建议\n\n");
                enhancedPromptBuilder.append("请在生成网站中合理使用以下资源，将这些图片合理地嵌入到网站相应位置");
                enhancedPromptBuilder.append(imageStr);
            }
            context.setCurrentStep("提示词增强");
            context.setEnhancedPrompt(enhancedPromptBuilder.toString());
            return WorkflowContext.saveContext(context);
        });
    }
}
