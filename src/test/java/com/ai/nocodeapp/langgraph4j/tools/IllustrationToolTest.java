package com.ai.nocodeapp.langgraph4j.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IllustrationToolTest {

    @Resource
    private IllustrationTool illustrationTool;

    @Test
    void testSearchIllustrations() {
        // 测试正常搜索插画
        List<String> illustrations = illustrationTool.searchIllustrations("happy");
        assertNotNull(illustrations);
    }
}
