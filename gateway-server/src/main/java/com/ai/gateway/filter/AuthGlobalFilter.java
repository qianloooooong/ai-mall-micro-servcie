package com.ai.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.ai.common.constants.XUserHeader.X_USER_ID;
import static com.ai.common.constants.XUserHeader.X_USER_ROLES;

@Configuration
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .cast(JwtAuthenticationToken.class)
                .map(auth -> {
                    Jwt jwt = auth.getToken();
                    String userId = jwt.getSubject();
                    List<String> roles = jwt.getClaimAsStringList("roles");

                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .headers(h -> {           // 先剥离客户端伪造的身份头
                                h.remove(X_USER_ID);
                                h.remove(X_USER_ROLES);
                            })
                            .header(X_USER_ID, userId == null ? "" : userId)
                            .header(X_USER_ROLES, roles == null ? "" : String.join(",", roles))
                            .build();
                    return exchange.mutate().request(request).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    @Override
    public int getOrder() {
        return 0; // 必须在 NettyRoutingFilter 之前
    }
}