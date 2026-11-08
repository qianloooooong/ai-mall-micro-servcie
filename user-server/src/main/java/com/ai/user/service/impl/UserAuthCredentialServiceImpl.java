package com.ai.user.service.impl;

import com.ai.user.mapper.UserAuthCredentialMapper;
import com.ai.user.pojo.entity.UserAuthCredential;
import com.ai.user.service.UserAuthCredentialService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserAuthCredentialServiceImpl extends ServiceImpl<UserAuthCredentialMapper, UserAuthCredential>
    implements UserAuthCredentialService{

}




