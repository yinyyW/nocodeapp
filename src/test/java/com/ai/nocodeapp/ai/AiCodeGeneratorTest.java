package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.core.AiCodeGeneratorFacade;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class AiCodeGeneratorTest {

    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    public void generateHtmlCodeTest() {
        String userMessage = "任务清单网页，代码不超过100行";
        File file = aiCodeGeneratorFacade.generateAndSaveCode(userMessage,
                CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    @Test
    public void generateMultifileCodeTest() {
        String userMessage = "任务清单网页，代码不超过100行";
        File file = aiCodeGeneratorFacade.generateAndSaveCode(userMessage,
                CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }
}
