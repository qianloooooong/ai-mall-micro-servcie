package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户凭证表
 * @TableName user_auth_credential
 */
@TableName(value ="user_auth_credential")
@Data
@Builder
public class UserAuthCredential {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * bcrypt 2b，单向
     */
    private String passwordHash;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

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