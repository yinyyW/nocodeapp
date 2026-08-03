package com.ai.nocodeapp.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.ai.nocodeapp.user.mapper")
@ComponentScan("com.ai")
public class AiNoCodeUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiNoCodeUserApplication.class, args);
    }
}
