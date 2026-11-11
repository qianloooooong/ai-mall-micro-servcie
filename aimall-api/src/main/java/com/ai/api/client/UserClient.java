package com.ai.api.client;

import com.ai.api.dto.UserLoginDTO;
import com.ai.common.results.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-server")
public interface UserClient {
    @PostMapping("/users/login/info")
    Result<UserLoginDTO> getUserLoginInfo(@RequestBody UserLoginDTO userLoginDTO);
}
