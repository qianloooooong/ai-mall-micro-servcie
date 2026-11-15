package com.ai.oauth.providers;

import com.ai.api.client.UserClient;
import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.LoginType;
import com.ai.common.results.Result;
import com.ai.oauth.domain.CustomUserDetails;
import com.ai.oauth.domain.LoginTypeWebAuthenticationDetails;
import com.ai.oauth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@RequiredArgsConstructor
public class VerificationCodeAuthenticationProvider implements AuthenticationProvider {

    private final UserClient userClient;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LoginTypeWebAuthenticationDetails details =
                (LoginTypeWebAuthenticationDetails) authentication.getDetails();
        if (details == null || !LoginType.isCodeLogin(details.getLoginType())) {
            return null;
        }

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername(authentication.getName());
        dto.setPassword(authentication.getCredentials().toString());
        dto.setLoginType(details.getLoginType());

        Result<UserLoginDTO> result = userClient.loginByCode(dto);
        UserLoginDTO info = result.getData();

        CustomUserDetails userDetails = UserService.toUserDetails(info);

        return new PreAuthenticatedAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
