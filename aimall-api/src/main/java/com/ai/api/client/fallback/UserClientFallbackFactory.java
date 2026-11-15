package com.ai.api.client.fallback;

import com.ai.api.client.UserClient;
import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.StatusCode;
import com.ai.common.results.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {
            @Override
            public Result<UserLoginDTO> getUserLoginInfo(UserLoginDTO userLoginDTO) {
                log.error("远程调用UserClient#getUserLoginInfo失败, args: {}, cause: {}", userLoginDTO, cause.getMessage());
                return Result.success(StatusCode.SUCCESS, new UserLoginDTO());
            }

            @Override
            public Result<UserLoginDTO> loginByCode(UserLoginDTO userLoginDTO) {
                log.error("远程调用UserClient#loginByCode失败, args: {}, cause: {}", userLoginDTO, cause.getMessage());
                return Result.success(StatusCode.SUCCESS, new UserLoginDTO());
            }
        };
    }
}
