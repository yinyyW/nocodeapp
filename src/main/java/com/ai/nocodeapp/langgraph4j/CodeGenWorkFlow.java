package com.ai.nocodeapp.langgraph4j;

import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.langgraph4j.model.QualityResult;
import com.ai.nocodeapp.langgraph4j.node.*;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

@Slf4j
public class CodeGenWorkFlow {

    public CompiledGraph<MessagesState<String>> createGraph() {
        try {
            return new MessagesStateGraph<String>()
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("router", RouterNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "router")
                    .addEdge("router", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    .addConditionalEdges("code_quality_check", edge_async(this::routeAfterQualityCheck),
                            Map.of("fail", "code_generator", "build", "project_builder", "skip_build", END))
                    .addEdge("project_builder", END)
                    .compile();
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建工作流失败");
        }
    }

    public WorkflowContext executeWorkflow(String originalPrompt) {
        CompiledGraph<MessagesState<String>> workflow = createGraph();
        // 初始化上下文状态
        WorkflowContext context = WorkflowContext.builder()
                .originalPrompt(originalPrompt).currentStep("初始化").build();

        // 执行工作流
        log.info("开始执行工作流");
        int stepCounter = 1;
        WorkflowContext finalContext = null;
        for (NodeOutput<MessagesState<String>> output : workflow.stream(Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, context))) {
            log.info("当前执行步骤：{}", stepCounter);
            // 获取当前状态
            WorkflowContext currentContext = WorkflowContext.getContext(output.state());
            if (currentContext != null) {
                finalContext = currentContext;
                log.info("当前工作流： {}", currentContext);
            }
            stepCounter++;
        }
        log.info("工作流执行完成");
        return finalContext;
    }

    private String routeBuildOrSkip(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        CodeGenTypeEnum generationType = context.getGenerationType();
        // HTML 和 MULTI_FILE 类型不需要构建，直接结束
        if (generationType == CodeGenTypeEnum.HTML || generationType == CodeGenTypeEnum.MULTI_FILE) {
            return "skip_build";
        }
        // VUE_PROJECT 需要构建
        return "build";
    }

    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        if (qualityResult == null || !qualityResult.getIsValid()) {
            return "fail";
        }
        return routeBuildOrSkip(state);
    }
}
