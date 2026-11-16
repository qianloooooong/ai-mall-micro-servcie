package com.ai.user.pojo.dto;

import lombok.Data;

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String emailCode;
    private String phone;
    private String phoneCode;
}
