package com.ai.user.service;

import com.ai.api.dto.UsernameLoginDTO;
import com.ai.user.pojo.entity.User;
import com.baomidou.mybatisplus.spring.service.IService;

public interface UserService extends IService<User> {

    UsernameLoginDTO getUserLoginInfo(UsernameLoginDTO usernameLoginDTO);
}
