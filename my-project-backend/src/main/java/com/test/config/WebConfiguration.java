package com.test.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {
    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
/*
RestTemplate 是Spring框架中的一个同步HTTP客户端，
用于执行HTTP请求，暴露了一系列的模板方法API，
便于操作底层的HTTP客户端库，
如JDK的HttpURLConnection、Apache HttpComponents等。RestTemplate通常作为共享组件使用，其配置不支持并发修改，
因此通常在启动时准备好配置。
如果需要，可以在启动时创建多个配置不同的RestTemplate实例。这些实例可以使用相同的底层ClientHttpRequestFactory，如果它们需要共享HTTP客户端资源。
 */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


//mybatis-plus内部实现分页功能
    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor(){
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor((DbType.MYSQL));
        paginationInnerInterceptor.setMaxLimit(100L);
        return paginationInnerInterceptor;
    }

}
