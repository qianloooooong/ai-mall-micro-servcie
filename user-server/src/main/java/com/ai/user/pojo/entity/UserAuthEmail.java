package com.ai.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户邮箱认证表
 * @TableName user_auth_email
 */
@TableName(value ="user_auth_email")
@Data
@Builder
public class UserAuthEmail {
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
     * 加密密钥版本
     */
    private String keyVersion;

    /**
     * 脱敏展示，如abc****@example.com
     */
    private String emailMasked;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 逻辑删除：0-未删除，id-已删除
     */
    @TableLogic(value = "0", delval = "id")
    private Long deleted;

    /**
     * 逻辑删除时间
     */
    private LocalDateTime deleteTime;

    /**
     * aes-256-gcm密文
     */
    private byte[] emailCipher;

    /**
     * hmac-sha256盲索引
     */
    private byte[] emailHash;
}