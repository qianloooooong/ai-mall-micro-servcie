package com.ai.api.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UserLoginDTO {
    private Long id;
    private String username;
    private String password;
    private String loginType;
    private Set<String> roles;
}
