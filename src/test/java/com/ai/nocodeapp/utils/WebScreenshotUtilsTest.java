package com.ai.nocodeapp.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String testUrl = "https://huggingface.co/shareAI";
        String webPageScreenshot = WebScreenshotUtils.saveScreenshot(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }
}
