package com.ai.user.service;

import com.ai.api.dto.UserLoginDTO;
import com.ai.user.pojo.dto.UserBindEmailDTO;
import com.ai.user.pojo.dto.UserBindPhoneDTO;
import com.ai.user.pojo.dto.UserRegisterDTO;
import com.ai.user.pojo.dto.UserResetPasswordDTO;
import com.ai.user.pojo.entity.User;
import com.baomidou.mybatisplus.spring.service.IService;

public interface UserService extends IService<User> {

    UserLoginDTO getUserLoginInfo(UserLoginDTO userLoginDTO);

    UserLoginDTO loginByCode(UserLoginDTO userLoginDTO);

    String bindPhone(UserBindPhoneDTO userBindPhoneDTO);

    String bindEmail(UserBindEmailDTO userBindEmailDTO);

    String resetPhone(UserBindPhoneDTO userBindPhoneDTO);

    String resetEmail(UserBindEmailDTO userBindEmailDTO);

    void sendPhoneCode(String phone);

    void sendEmailCode(String email);

    User register(UserRegisterDTO userRegisterDTO);

    void resetPassword(UserResetPasswordDTO userResetPasswordDTO);

    void resetNickname(String nickname);

    void checkUsername(String username);
}
