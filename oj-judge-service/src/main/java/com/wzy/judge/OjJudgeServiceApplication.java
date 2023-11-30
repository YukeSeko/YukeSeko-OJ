package com.wzy.judge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author YukeSeko
 * @Since 2023/10/3 11:12
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.wzy.common.feign")
public class OjJudgeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OjJudgeServiceApplication.class, args);
    }
}
