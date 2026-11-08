package com.ai.user.config;

import com.ai.common.filter.XUserRolesHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 微服务无状态，禁用 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                // 所有请求都放行到业务层，由方法鉴权做细粒度控制；或根据需要配置入口规则
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 将自定义过滤器插入到 BasicAuthenticationFilter 之前
                .addFilterBefore(new XUserRolesHeaderFilter(), BasicAuthenticationFilter.class);

        return http.build();
    }
}
