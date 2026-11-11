package com.ai.user.controller;

import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.StatusCode;
import com.ai.common.results.Result;
import com.ai.common.utils.UserContext;
import com.ai.user.pojo.dto.UserBindEmailDTO;
import com.ai.user.pojo.dto.UserBindPhoneDTO;
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
    public Result<UserLoginDTO> getUserLoginInfo(@RequestBody UserLoginDTO userLoginDTO) {
        return Result.success(StatusCode.SUCCESS_GET_USER_LOGIN_INFO, userService.getUserLoginInfo(userLoginDTO));
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/phone/bind")
    public Result<String> bindPhone(@RequestBody UserBindPhoneDTO userBindPhoneDTO) {
        String maskedPhone = userService.bindPhone(userBindPhoneDTO);
        return Result.success(StatusCode.SUCCESS_BIND_PHONE, maskedPhone);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/email/bind")
    public Result<String> bindEmail(@RequestBody UserBindEmailDTO userBindEmailDTO) {
        String maskedEmail = userService.bindEmail(userBindEmailDTO);
        return Result.success(StatusCode.SUCCESS_BIND_EMAIL, maskedEmail);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/phone/reset")
    public Result<String> resetPhone(@RequestBody UserBindPhoneDTO userBindPhoneDTO) {
        String maskedPhone = userService.resetPhone(userBindPhoneDTO);
        return Result.success(StatusCode.SUCCESS_RESET_PHONE, maskedPhone);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/email/reset")
    public Result<String> resetEmail(@RequestBody UserBindEmailDTO userBindEmailDTO) {
        String maskedEmail = userService.resetEmail(userBindEmailDTO);
        return Result.success(StatusCode.SUCCESS_RESET_EMAIL, maskedEmail);
    }

    @PostMapping("/phone/code")
    public Result<Void> sendPhoneCode(@RequestParam String phone) {
        userService.sendPhoneCode(phone);
        return Result.success(StatusCode.SUCCESS_SEND_PHONE_CODE);
    }

    @PostMapping("/email/code")
    public Result<Void> sendEmailCode(@RequestParam String email) {
        userService.sendEmailCode(email);
        return Result.success(StatusCode.SUCCESS_SEND_EMAIL_CODE);
    }



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
}
