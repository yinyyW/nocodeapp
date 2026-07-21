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
        WorkflowContext result = new CodeGenWorkFlow().executeWorkflow("设计一个个人博客");
        Assertions.assertNotNull(result);
    }
}