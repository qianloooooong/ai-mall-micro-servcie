package com.ai.common.interceptor;

import com.ai.common.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ai.common.constants.XUserHeader.X_USER_ID;
import static com.ai.common.constants.XUserHeader.X_USER_ROLES;

public class ContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader(X_USER_ID);
        String roles = request.getHeader(X_USER_ROLES);
        if (StringUtils.hasText(userId)) {
            UserContext.setUser(userId, parseRoles(roles));
        }
        return true;
    }

    private Set<String> parseRoles(String roles) {
        if (!StringUtils.hasText(roles)) {
            return Collections.emptySet();
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());   // Collectors.toSet 允许重复元素，不会像 Set.of 那样抛异常
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.remove();
    }
}
