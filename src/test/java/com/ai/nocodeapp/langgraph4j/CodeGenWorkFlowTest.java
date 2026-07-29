package com.ai.nocodeapp.langgraph4j;

import com.ai.nocodeapp.langgraph4j.state.WorkflowContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeGenWorkFlowTest {

    @Test
    void executeWorkflow() {
        WorkflowContext result = new CodeGenWorkFlow().executeWorkflow("创建一个企业官网，展示公司形象和业务介绍");
        Assertions.assertNotNull(result);
    }
}