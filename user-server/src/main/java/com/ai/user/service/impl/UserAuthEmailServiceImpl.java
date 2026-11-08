package com.ai.user.service.impl;

import com.ai.user.mapper.UserAuthEmailMapper;
import com.ai.user.pojo.entity.UserAuthEmail;
import com.ai.user.service.UserAuthEmailService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserAuthEmailServiceImpl extends ServiceImpl<UserAuthEmailMapper, UserAuthEmail>
    implements UserAuthEmailService{

}




