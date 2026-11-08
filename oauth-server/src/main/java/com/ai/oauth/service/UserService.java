package com.ai.oauth.service;

import cn.hutool.core.util.StrUtil;
import com.ai.api.client.UserClient;
import com.ai.api.dto.UsernameLoginDTO;
import com.ai.oauth.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsernameLoginDTO usernameLoginDTO = new UsernameLoginDTO();
        usernameLoginDTO.setUsername(username);
        UsernameLoginDTO userLoginInfo = userClient.getUserLoginInfo(usernameLoginDTO).getData();

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
