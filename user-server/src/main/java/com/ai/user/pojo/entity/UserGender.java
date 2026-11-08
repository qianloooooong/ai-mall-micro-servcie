package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 性别表
 * @TableName user_gender
 */
@TableName(value ="user_gender")
@Data
public class UserGender {
    /**
     * 用户性别ID
     */
    @TableId
    private Integer id;

    /**
     * 性别
     */
    private String gender;
}