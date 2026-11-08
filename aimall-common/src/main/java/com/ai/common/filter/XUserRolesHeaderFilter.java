package com.ai.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class XUserRolesHeaderFilter extends OncePerRequestFilter {

    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {
        String rolesHeader = request.getHeader(ROLES_HEADER);

        // 仅当存在角色头，且当前线程尚未被认证时，才进行填充，避免覆盖已有认证
        if (rolesHeader != null && !rolesHeader.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 关键工具：AuthorityUtils 直接将逗号分隔字符串转为 GrantedAuthority 列表
            List<GrantedAuthority> authorities =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(rolesHeader);

            // 构建一个“已认证”的 Authentication 对象
            String userId = request.getHeader(USER_ID_HEADER);
            // Principal 可以暂用角色字符串或下游服务自己的用户标识（如 X-User-Id）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 无论是否填充，都必须放行，让后续的鉴权逻辑（或匿名访问）自行判断
        filterChain.doFilter(request, response);
    }
}