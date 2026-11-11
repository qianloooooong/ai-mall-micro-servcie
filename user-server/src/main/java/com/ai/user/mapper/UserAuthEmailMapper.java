package com.ai.user.mapper;

import com.ai.user.pojo.entity.UserAuthEmail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface UserAuthEmailMapper extends BaseMapper<UserAuthEmail> {

    void deleteAuthEmail(Long userId, LocalDateTime now);
}




