package com.ai.nocodeapp.langgraph4j.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImages() {
        // 测试正常搜索插画
        List<String> images = imageSearchTool.searchImages("technology");
        assertNotNull(images);
    }
}