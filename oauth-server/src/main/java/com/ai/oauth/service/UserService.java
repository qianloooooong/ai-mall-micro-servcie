package com.ai.oauth.service;

import cn.hutool.core.util.StrUtil;
import com.ai.api.client.UserClient;
import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.LoginType;
import com.ai.oauth.domain.CustomUserDetails;
import com.ai.oauth.domain.LoginTypeWebAuthenticationDetails;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserLoginDTO userLoginDTO = UserLoginDTO.builder()
                .username(username)
                .loginType(resolveLoginType())
                .build();

        UserLoginDTO userLoginInfo = null;
        try {
            userLoginInfo = userClient.getUserLoginInfo(userLoginDTO).getData();
        } catch (FeignException e) {
            throw new UsernameNotFoundException("用户不存在或凭据无效", e);
        }
        if (userLoginInfo == null) {
            throw new UsernameNotFoundException("用户不存在或凭据无效");
        }

        return toUserDetails(userLoginInfo);
    }

    private String resolveLoginType() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return LoginType.USERNAME_PASSWORD.getType();
        }
        Object loginType = attrs.getAttribute(
                LoginTypeWebAuthenticationDetails.LOGIN_TYPE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return loginType instanceof String s ? s : LoginType.USERNAME_PASSWORD.getType();
    }

    public static CustomUserDetails toUserDetails(UserLoginDTO userLoginInfo) {
        CustomUserDetails userDetails = new CustomUserDetails();
        userDetails.setUserId(userLoginInfo.getId() == null ? null : String.valueOf(userLoginInfo.getId()));
        userDetails.setUsername(userLoginInfo.getUsername());
        userDetails.setPassword(userLoginInfo.getPassword());
        userDetails.setAuthorities(
                userLoginInfo.getRoles()
                .stream()
                        .filter(StrUtil::isNotBlank)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet())
        );
        return userDetails;
    }
}
