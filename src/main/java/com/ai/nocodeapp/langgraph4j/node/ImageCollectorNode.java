package com.ai.nocodeapp.langgraph4j.node;

import com.ai.nocodeapp.langgraph4j.ai.ImageCollectionService;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行图片收集节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            // 获取插画
            ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
            context.setCurrentStep("获取图片");
            String imageStr = imageCollectionService.searchIllustrations(originalPrompt);
            context.setImageListStr(imageStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
