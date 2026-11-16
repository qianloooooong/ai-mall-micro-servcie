package com.ai.user.pojo.dto;

import lombok.Data;

@Data
public class UserResetPasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
    private String phone;
    private String phoneCode;
    private String email;
    private String emailCode;
}
