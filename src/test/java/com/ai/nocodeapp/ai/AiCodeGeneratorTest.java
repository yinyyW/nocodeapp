package com.ai.nocodeapp.ai;

import com.ai.nocodeapp.ai.model.MultiFileCodeResult;
import com.ai.nocodeapp.core.AiCodeGeneratorFacade;
import com.ai.nocodeapp.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
public class AiCodeGeneratorTest {

    @Resource
    AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    public void generateHtmlCodeTest() {
        String userMessage = "英语学习导航页，代码不超过100行";
        File file = aiCodeGeneratorFacade.generateAndSaveCode(1L, userMessage,
                CodeGenTypeEnum.HTML);
        Assertions.assertNotNull(file);
    }

    @Test
    public void generateMultifileCodeTest() {
        String userMessage = "任务清单网页，代码不超过100行";
        File file = aiCodeGeneratorFacade.generateAndSaveCode(1L, userMessage,
                CodeGenTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateHtmlCodeStreamTest() {
        Flux<String> codeStream =
                aiCodeGeneratorFacade.generateAndSaveCodeStream(1L, "任务记录网站",
                        CodeGenTypeEnum.HTML);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


    @Test
    void generateMultiFileCodeStreamTest() {
        Flux<String> codeStream =
                aiCodeGeneratorFacade.generateAndSaveCodeStream(1L, "任务记录网站",
                        CodeGenTypeEnum.MULTI_FILE);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(1L,
                "简单的任务记录网站，总代码量不超过 200 行",
                CodeGenTypeEnum.VUE_PROJECT);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }


}
