package com.ai.nocodeapp.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理
 */
@Slf4j
@Component
public class ToolManager {

    /**
     * 工具映射：工具名 -> 工具对象
     */
    private Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 所有工具
     */
    @Resource
    private BaseTool[] tools;

    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
        }
        log.info("工具管理器初始化完成，注册 {} 工具", toolMap.size());
    }

    /**
     * 根据工具名获取工具实例
     * @param toolName 工具名称
     * @return 工具实例
     */
    public BaseTool getToolByName(String toolName) {
        return toolMap.get(toolName);
    }

    public BaseTool[] getAllTools() {
        return tools;
    }
}
