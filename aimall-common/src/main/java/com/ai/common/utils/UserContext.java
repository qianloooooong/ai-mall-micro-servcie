package com.ai.common.utils;

import java.util.Collections;
import java.util.Set;

public class UserContext {
    public static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    public static final ThreadLocal<Set<String>> ROLES = new ThreadLocal<>();

    public static void setUser(String userId, Set<String> roles) {
        USER_ID.set(Long.valueOf(userId));
        ROLES.set(roles);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static Set<String> getRoles() {
        Set<String> roles = ROLES.get();
        return roles == null ? Collections.emptySet() : roles;
    }

    public static void remove() {
        USER_ID.remove();
        ROLES.remove();
    }
}
