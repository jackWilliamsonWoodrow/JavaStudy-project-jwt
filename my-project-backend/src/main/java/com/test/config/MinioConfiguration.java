package com.test.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
设置minio的初始化参数
endpoint:客户端地址
用户名和密码
 */
@Slf4j
@Configuration
public class MinioConfiguration {

    @Value("${spring.minio.endpoint}")
    String endpoint;

    @Value("${spring.minio.username}")
    String username;

    @Value("${spring.minio.password}")
    String password;

    @Bean
    public MinioClient minioClient(){
        log.info("Init minio client...");
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username,password)
                .build();
    }
}
