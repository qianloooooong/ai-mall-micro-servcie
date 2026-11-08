package com.ai.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.ai.api.dto.UsernameLoginDTO;
import com.ai.common.enums.UserStatus;
import com.ai.common.exception.UserException;
import com.ai.user.hmac.HmacBlindIndex;
import com.ai.user.keyprovider.KeyProvider;
import com.ai.user.mapper.UserMapper;
import com.ai.user.mapper.UserRoleMapper;
import com.ai.user.pojo.entity.User;
import com.ai.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ai.common.enums.StatusCode.UNAUTHORIZED_LOGIN_INFO_ERROR;
import static com.ai.common.enums.StatusCode.USERNAME_ARGUMENT_NULL;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final HmacBlindIndex hmacBlindIndex;

    private final KeyProvider keyProvider;

    private final UserMapper userMapper;
    
    private final UserRoleMapper userRoleMapper;

    @Override
    public UsernameLoginDTO getUserLoginInfo(UsernameLoginDTO usernameLoginDTO) {
        String username = usernameLoginDTO.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new UserException(USERNAME_ARGUMENT_NULL);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("u.username", username);
        wrapper.eq("u.status", UserStatus.ENABLE.getCode());

        UsernameLoginDTO loginInfo = userMapper.getUserLoginInfo(wrapper);

        if (loginInfo == null) {
            throw new UserException(UNAUTHORIZED_LOGIN_INFO_ERROR);
        }

        List<String> list = userRoleMapper.getRoles(loginInfo.getId());
        Set<String> roles = new HashSet<>(list);
        loginInfo.setRoles(roles);

        return loginInfo;
    }
}




