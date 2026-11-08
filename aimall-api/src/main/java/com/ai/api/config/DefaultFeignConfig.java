package com.ai.api.config;

import com.ai.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Set;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userId = UserContext.getUserId();
                Set<String> roles = UserContext.getRoles();
                if (userId != null && !roles.isEmpty()) {
                    requestTemplate.header("X-User-Id", userId.toString());
                    requestTemplate.header("X-User-Roles", String.join(",", roles));
                }
            }
        };
    }
}