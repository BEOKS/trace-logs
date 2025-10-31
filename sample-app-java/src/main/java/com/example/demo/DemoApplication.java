package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 순수 Java로 작성된 Spring Boot 애플리케이션
 * TraceLens (Kotlin 라이브러리)를 사용하여 로그 스트리밍 기능 제공
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
