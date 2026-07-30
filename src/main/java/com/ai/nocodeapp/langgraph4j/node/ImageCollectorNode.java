package com.ai.nocodeapp.langgraph4j.node;

import com.ai.nocodeapp.langgraph4j.ai.ImageCollectionPlanService;
import com.ai.nocodeapp.langgraph4j.ai.ImageCollectionService;
import com.ai.nocodeapp.langgraph4j.model.ImageCollectionPlan;
import com.ai.nocodeapp.langgraph4j.model.ImageResource;
import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import com.ai.nocodeapp.langgraph4j.tools.IllustrationTool;
import com.ai.nocodeapp.langgraph4j.tools.ImageSearchTool;
import com.ai.nocodeapp.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            log.info("执行图片收集节点");
            WorkflowContext context = WorkflowContext.getContext(state);
            String originalPrompt = context.getOriginalPrompt();
            List<ImageResource> collectedImages = new ArrayList<>();
            try {
                // AI 获取图片搜索参数
                ImageCollectionPlanService imageCollectionPlanService = SpringContextUtil.getBean(ImageCollectionPlanService.class);
                ImageCollectionPlan imageCollectionPlan = imageCollectionPlanService.planImageCollection(originalPrompt);
                List<CompletableFuture<List<ImageResource>>> futures = new ArrayList<>();
                if (imageCollectionPlan.getContentImageTasks() != null && !imageCollectionPlan.getContentImageTasks().isEmpty()) {
                    ImageSearchTool imageSearchTool = SpringContextUtil.getBean(ImageSearchTool.class);
                    for (ImageCollectionPlan.ImageSearchTask task : imageCollectionPlan.getContentImageTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                            imageSearchTool.searchImages(task.query())));
                    }
                }
                // AI插画搜索参数
                if (imageCollectionPlan.getIllustrationTasks() != null && !imageCollectionPlan.getIllustrationTasks().isEmpty()) {
                    IllustrationTool illustrationTool = SpringContextUtil.getBean(IllustrationTool.class);
                    for (ImageCollectionPlan.IllustrationTask task : imageCollectionPlan.getIllustrationTasks()) {
                        futures.add(CompletableFuture.supplyAsync(() ->
                                illustrationTool.searchIllustrations(task.query())));
                    }
                }
                // 等待所有异步操作完成
                CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allTasks.join();
                // 获取异步操作结果
                for (CompletableFuture<List<ImageResource>> future: futures) {
                    List<ImageResource> images = future.get();
                    if (images != null) {
                        collectedImages.addAll(images);
                    }
                }
            } catch (Exception e) {
                log.error("获取图片失败：{}", e.getMessage(), e);
            }
            context.setCurrentStep("图片收集");
            context.setImageResourceList(collectedImages);
            return WorkflowContext.saveContext(context);
        });
    }
}
