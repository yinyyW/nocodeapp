package com.ai.nocodeapp.langgraph4j;

import com.ai.nocodeapp.langgraph4j.ai.ImageCollectionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
class ImageCollectionServiceTest {

    @Resource
    private ImageCollectionService imageCollectionService;

    @Test
    void createImageCollectionService() {
        String images = imageCollectionService.searchIllustrations("创建一个科技企业网站");
        log.info("收集图片：{}", images);
        Assertions.assertNotNull(images);
    }
}