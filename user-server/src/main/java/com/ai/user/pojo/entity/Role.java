package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色表
 * @TableName role
 */
@TableName(value ="role")
@Data
public class Role {
    /**
     * 用户角色ID
     */
    @TableId
    private Integer id;

    /**
     * 角色
     */
    private String role;

    /**
     * 描述
     */
    private String description;
}