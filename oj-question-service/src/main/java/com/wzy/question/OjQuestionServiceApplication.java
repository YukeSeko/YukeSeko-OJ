package com.wzy.question;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author YukeSeko
 * @Since 2023/10/7 9:51
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.wzy.question.mapper")
@EnableFeignClients(basePackages = "com.wzy.common.feign")
public class OjQuestionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjQuestionServiceApplication.class, args);
    }
}
