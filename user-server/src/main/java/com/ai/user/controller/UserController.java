package com.ai.user.controller;

import com.ai.api.dto.UsernameLoginDTO;
import com.ai.common.enums.StatusCode;
import com.ai.common.results.Result;
import com.ai.common.utils.UserContext;
import com.ai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static com.ai.common.enums.StatusCode.SUCCESS_WITH_DATA;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/login/info")
    public Result<UsernameLoginDTO> getUserLoginInfo(@RequestBody UsernameLoginDTO usernameLoginDTO) {
        return Result.success(StatusCode.SUCCESS_POST_WITH_DATA, userService.getUserLoginInfo(usernameLoginDTO));
    }
//    @Autowired
//    private AseKeyProviderConfig config;
//    @Autowired
//    private NacosKeyProvider nacosKeyProvider;

    @GetMapping("/hi")
    public Result<String> hi() {
        Long userId = UserContext.getUserId();
        Set<String> roles = UserContext.getRoles();
        log.info("User ID: {}", userId);
        log.info("User Roles: {}", roles);
        return Result.success(SUCCESS_WITH_DATA, userId + " " + roles);
        //return Result.error(50001, "error");
    }

    @GetMapping("/auth")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Result<String> testAuth() {
        return Result.success(SUCCESS_WITH_DATA, "auth success");
    }
//
//    @GetMapping("/config")
//    public Result<String> getConfig() {
//        return Result.success(SUCCESS_WITH_DATA, config.getCurrentVersion());
//    }
//
//    @GetMapping("/key/{version}")
//    public Result<String> getKey(@PathVariable String version) {
//        return Result.success(SUCCESS_WITH_DATA, nacosKeyProvider.getAesKey(version).toString());
//    }
}
