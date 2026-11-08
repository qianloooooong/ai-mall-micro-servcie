package com.ai.api.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UsernameLoginDTO {
    private Long id;
    private String username;
    private String password;
    private Set<String> roles;
}
