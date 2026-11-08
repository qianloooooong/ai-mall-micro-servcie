package com.ai.user.service.impl;

import com.ai.user.mapper.RoleMapper;
import com.ai.user.pojo.entity.Role;
import com.ai.user.service.RoleService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role>
    implements RoleService{

}




