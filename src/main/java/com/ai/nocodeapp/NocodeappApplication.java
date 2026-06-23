package com.ai.nocodeapp;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.ai.nocodeapp.mapper")
@SpringBootApplication
public class NocodeappApplication {

    public static void main(String[] args) {
        SpringApplication.run(NocodeappApplication.class, args);
    }

}
