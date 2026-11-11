package com.ai.api.dto;

import com.ai.common.enums.LoginType;
import lombok.Data;

import java.util.Set;

@Data
public class UserLoginDTO {
    private Long id;
    private String username;
    private String password;
    private LoginType loginType;
    private Set<String> roles;
}
