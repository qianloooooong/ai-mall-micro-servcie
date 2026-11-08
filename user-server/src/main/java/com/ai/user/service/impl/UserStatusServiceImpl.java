package com.ai.user.service.impl;

import com.ai.user.mapper.UserStatusMapper;
import com.ai.user.pojo.entity.UserStatus;
import com.ai.user.service.UserStatusService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserStatusServiceImpl extends ServiceImpl<UserStatusMapper, UserStatus>
    implements UserStatusService{

}




