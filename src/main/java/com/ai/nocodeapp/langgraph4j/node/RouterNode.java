package com.ai.nocodeapp.langgraph4j.node;

import com.ai.nocodeapp.ai.AiCodeGenTypeRoutingService;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.model.dto.CodeGenTypeRoutingResult;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class RouterNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("智能路由节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();

            // 获取智能路由服务
            context.setCurrentStep("智能路由");
            try {
                AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = SpringContextUtil.getBean(AiCodeGenTypeRoutingService.class);
                CodeGenTypeRoutingResult result = aiCodeGenTypeRoutingService.routeCodeGenType(originalPrompt);
                context.setGenerationType(result.getCodeGenerationType());
            } catch (Exception e) {
                log.error("AI 智能路由失败，选择类型 {}", CodeGenTypeEnum.HTML.getText());
                context.setGenerationType(CodeGenTypeEnum.HTML);
            }
            return WorkflowContext.saveContext(context);
        });
    }
}
