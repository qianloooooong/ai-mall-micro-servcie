package com.ai.oauth.domain;

import com.ai.common.enums.LoginType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

@Getter
public class LoginTypeWebAuthenticationDetails extends WebAuthenticationDetails {
    public static final String LOGIN_TYPE_ATTRIBUTE = "LOGIN_TYPE";

    private final String loginType;

    public LoginTypeWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        String loginType = request.getParameter("loginType");
        loginType = loginType == null ? LoginType.USERNAME_PASSWORD.getType() : loginType;
        this.loginType = loginType;
        request.setAttribute(LOGIN_TYPE_ATTRIBUTE, loginType);
    }
}
