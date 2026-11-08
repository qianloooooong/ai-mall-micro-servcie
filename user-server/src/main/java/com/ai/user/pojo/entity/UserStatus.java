package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户状态表
 * @TableName user_status
 */
@TableName(value ="user_status")
@Data
public class UserStatus {
    /**
     * 用户状态ID
     */
    @TableId
    private Integer id;

    /**
     * 状态
     */
    private String status;
}