package com.ai.nocodeapp.langgraph4j.node;

import com.ai.nocodeapp.constants.AppConstant;
import com.ai.nocodeapp.core.builder.VueProjectBuilder;
import com.ai.nocodeapp.exception.BusinessException;
import com.ai.nocodeapp.exception.ErrorCode;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import com.ai.nocodeapp.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.File;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ProjectBuilderNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("项目构建节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String codeDir = context.getGeneratedCodeDir();

            // 项目构建
            context.setCurrentStep("项目构建");
            String buildDir;
            try {
                VueProjectBuilder vueProjectBuilder = SpringContextUtil.getBean(VueProjectBuilder.class);
                boolean buildResult = vueProjectBuilder.buildProject(codeDir);
                if (buildResult) {
                    buildDir = codeDir + File.separator + "dist";
                } else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "项目构建失败");
                }
            } catch (Exception e) {
                // 报错默认使用代码生成目录
                log.error("项目构建失败", e);
                buildDir = codeDir;
            }
            context.setBuildResultDir(buildDir);
            return WorkflowContext.saveContext(context);
        });
    }
}
