package com.ai.user.service;

import com.ai.api.dto.UserLoginDTO;
import com.ai.user.pojo.dto.UserBindEmailDTO;
import com.ai.user.pojo.dto.UserBindPhoneDTO;
import com.ai.user.pojo.entity.User;
import com.baomidou.mybatisplus.spring.service.IService;

public interface UserService extends IService<User> {

    UserLoginDTO getUserLoginInfo(UserLoginDTO userLoginDTO);

    String bindPhone(UserBindPhoneDTO userBindPhoneDTO);

    String bindEmail(UserBindEmailDTO userBindEmailDTO);

    String resetPhone(UserBindPhoneDTO userBindPhoneDTO);

    String resetEmail(UserBindEmailDTO userBindEmailDTO);

    void sendPhoneCode(String phone);

    void sendEmailCode(String email);
}
