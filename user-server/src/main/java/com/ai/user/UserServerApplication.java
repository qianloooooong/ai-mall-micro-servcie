package com.ai.user;

import com.ai.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@Import({com.ai.common.results.ResultHttpStatusAdvice.class,
        com.ai.common.config.MvcConfig.class,
        com.ai.common.handler.GlobalExceptionHandler.class})
@EnableFeignClients(basePackages = "com.ai.api.client", defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class UserServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServerApplication.class, args);
    }

}
