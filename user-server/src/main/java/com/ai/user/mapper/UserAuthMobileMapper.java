package com.ai.user.mapper;

import com.ai.user.pojo.entity.UserAuthMobile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface UserAuthMobileMapper extends BaseMapper<UserAuthMobile> {

    void deleteAuthMobile(Long userId, LocalDateTime now);
}




