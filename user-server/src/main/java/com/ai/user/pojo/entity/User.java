package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
@Builder
public class User {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户唯一编号
     */
    private Long userNo;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：0-未设置，1-男，2-女
     */
    private Integer gender;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 账号状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 逻辑删除：0-未删除，id-已删除
     */
    @TableLogic(value = "0", delval = "id")
    private Long deleted;

    /**
     * 逻辑删除时间
     */
    private LocalDateTime deleteTime;
}