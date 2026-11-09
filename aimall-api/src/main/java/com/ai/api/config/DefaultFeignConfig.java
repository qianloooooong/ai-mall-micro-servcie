package com.ai.api.config;

import com.ai.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Set;

import static com.ai.common.constants.XUserHeaderConstants.X_USER_ID;
import static com.ai.common.constants.XUserHeaderConstants.X_USER_ROLES;

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
                    requestTemplate.header(X_USER_ID, userId.toString());
                    requestTemplate.header(X_USER_ROLES, String.join(",", roles));
                }
            }
        };
    }
}