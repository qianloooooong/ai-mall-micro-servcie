package com.ai.user.mapper;

import com.ai.api.dto.UsernameLoginDTO;
import com.ai.user.pojo.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    UsernameLoginDTO getUserLoginInfo(@Param(Constants.WRAPPER) QueryWrapper<User> ew);
}




