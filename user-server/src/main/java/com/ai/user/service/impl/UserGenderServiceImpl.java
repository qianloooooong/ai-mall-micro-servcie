package com.ai.user.service.impl;

import com.ai.user.mapper.UserGenderMapper;
import com.ai.user.pojo.entity.UserGender;
import com.ai.user.service.UserGenderService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserGenderServiceImpl extends ServiceImpl<UserGenderMapper, UserGender>
    implements UserGenderService{

}




