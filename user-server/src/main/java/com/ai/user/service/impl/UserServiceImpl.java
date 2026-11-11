package com.ai.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.LogicDeleted;
import com.ai.common.enums.UserStatus;
import com.ai.common.exception.UserException;
import com.ai.common.utils.UserContext;
import com.ai.common.utils.Validators;
import com.ai.user.crypto.AesGcmCrypto;
import com.ai.user.hmac.HmacBlindIndex;
import com.ai.user.keyprovider.KeyProvider;
import com.ai.user.mapper.UserAuthEmailMapper;
import com.ai.user.mapper.UserAuthMobileMapper;
import com.ai.user.mapper.UserMapper;
import com.ai.user.mapper.UserRoleMapper;
import com.ai.user.pojo.dto.UserBindEmailDTO;
import com.ai.user.pojo.dto.UserBindPhoneDTO;
import com.ai.user.pojo.entity.User;
import com.ai.user.pojo.entity.UserAuthEmail;
import com.ai.user.pojo.entity.UserAuthMobile;
import com.ai.user.service.UserAuthEmailService;
import com.ai.user.service.UserAuthMobileService;
import com.ai.user.service.UserService;
import com.ai.user.utils.CodeUtils;
import com.ai.user.utils.MaskedUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ai.common.constants.RedisConstants.EMAIL_CODE;
import static com.ai.common.constants.RedisConstants.PHONE_CODE;
import static com.ai.common.enums.StatusCode.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    private final HmacBlindIndex hmacBlindIndex;

    private final AesGcmCrypto aesGcmCrypto;

    private final KeyProvider keyProvider;

    private final StringRedisTemplate redisTemplate;

    private final UserMapper userMapper;
    
    private final UserRoleMapper userRoleMapper;

    private final UserAuthMobileService userAuthMobileService;

    private final UserAuthMobileMapper userAuthMobileMapper;

    private final UserAuthEmailService userAuthEmailService;

    private final UserAuthEmailMapper userAuthEmailMapper;

    private final CodeUtils codeUtils;

    @Override
    public UserLoginDTO getUserLoginInfo(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new UserException(NULL_USERNAME);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("u.username", username);
        wrapper.eq("u.status", UserStatus.ENABLE.getCode());
        wrapper.eq("u.deleted", LogicDeleted.NOT_DELETED.getCode());
        wrapper.eq("uac.deleted", LogicDeleted.NOT_DELETED.getCode());

        UserLoginDTO loginInfo = userMapper.getUserLoginInfo(wrapper);

        if (loginInfo == null) {
            throw new UserException(UNAUTHORIZED_LOGIN_INFO_ERROR);
        }

        List<String> list = userRoleMapper.getRoles(loginInfo.getId());
        Set<String> roles = new HashSet<>(list);
        loginInfo.setRoles(roles);

        return loginInfo;
    }

    @Override
    public String bindPhone(UserBindPhoneDTO userBindPhoneDTO) {
        Long userId = UserContext.getUserId();
        String phone = userBindPhoneDTO.getPhone();
        String code = userBindPhoneDTO.getCode();

        checkPhone(phone, userId);

        checkPhoneCode(phone, code, userId);

        String maskedPhone = MaskedUtil.maskedPhone(phone);

        Long count = userAuthMobileService.lambdaQuery()
                .eq(UserAuthMobile::getUserId, userId)
                .eq(UserAuthMobile::getDeleted, LogicDeleted.NOT_DELETED.getCode())
                .count();
        if (count > 0) {
            throw new UserException(BIND_PHONE_NOT_FIRST, "UserId=" + userId);
        }

        insertAuthMobile(userId, maskedPhone, phone);

        return maskedPhone;
    }

    @Override
    @Transactional
    public String resetPhone(UserBindPhoneDTO userBindPhoneDTO) {
        Long userId = UserContext.getUserId();
        String phone = userBindPhoneDTO.getPhone();
        String code = userBindPhoneDTO.getCode();

        checkPhone(phone, userId);

        checkPhoneCode(phone, code, userId);

        String maskedPhone = MaskedUtil.maskedPhone(phone);

        deleteAuthMobile(userId);

        insertAuthMobile(userId, maskedPhone, phone);

        return maskedPhone;
    }

    private void checkPhoneCode(String phone, String code, Long userId) {
        Long check = codeUtils.verify(PHONE_CODE + phone, code);
        if (check.equals(CodeUtils.NULL)) {
            throw new UserException(INVALID_PHONE_CODE, "UserId=" + userId);
        } else if (check.equals(CodeUtils.FALSE)) {
            throw new UserException(ERROR_PHONE_CODE, "UserId=" + userId);
        }
    }

    private void checkPhone(String phone, Long userId) {
        if (StrUtil.isBlank(phone)) {
            throw new UserException(NULL_USER_BIND_PHONE);
        } else if (!Validators.isCnMobile(phone)) {
            throw new UserException(INVALID_USER_BIND_PHONE, "UserId=" + userId);
        }
    }

    private void insertAuthMobile(Long userId, String maskedPhone, String phone) {
        String aesCurrentVersion = keyProvider.aesCurrentVersion();
        String hmacCurrentVersion = keyProvider.hmacCurrentVersion();
        SecretKey aesKey = keyProvider.getAesKey(aesCurrentVersion);
        SecretKey hmacKey = keyProvider.getHmacKey(hmacCurrentVersion);

        UserAuthMobile userAuthMobile = UserAuthMobile.builder()
                .userId(userId)
                .mobileMasked(maskedPhone)
                .mobileCipher(aesGcmCrypto.encrypt(phone, aesKey))
                .mobileHash(hmacBlindIndex.compute(phone, hmacKey))
                .keyVersion(aesCurrentVersion)
                .createTime(LocalDateTime.now())
                .build();

        try {
            userAuthMobileMapper.insert(userAuthMobile);
        }catch (DuplicateKeyException e) {
            throw new UserException(PHONE_EXISTED, "UserId=" + userId);
        }catch (Exception e) {
            throw new UserException(BIND_PHONE_FAILURE, "UserId=" + userId);
        }
    }

    private void deleteAuthMobile(Long userId) {
        userAuthMobileMapper.deleteAuthMobile(userId, LocalDateTime.now());
    }

    @Override
    public String bindEmail(UserBindEmailDTO userBindEmailDTO) {
        Long userId = UserContext.getUserId();
        String email = userBindEmailDTO.getEmail();
        String code = userBindEmailDTO.getCode();

        checkEmail(email, userId);

        checkEmailCode(email, code, userId);

        String maskedEmail = MaskedUtil.maskedEmail(email);

        Long count = userAuthEmailService.lambdaQuery()
                .eq(UserAuthEmail::getUserId, userId)
                .eq(UserAuthEmail::getDeleted, LogicDeleted.NOT_DELETED.getCode())
                .count();
        if (count > 0) {
            throw new UserException(BIND_EMAIL_NOT_FIRST, "UserId=" + userId);
        }

        insertAuthEmail(userId, maskedEmail, email);

        return maskedEmail;
    }

    @Override
    @Transactional
    public String resetEmail(UserBindEmailDTO userBindEmailDTO) {
        Long userId = UserContext.getUserId();
        String email = userBindEmailDTO.getEmail();
        String code = userBindEmailDTO.getCode();

        checkEmail(email, userId);

        checkEmailCode(email, code, userId);

        String maskedEmail = MaskedUtil.maskedEmail(email);

        deleteAuthEmail(userId);

        insertAuthEmail(userId, maskedEmail, email);

        return maskedEmail;
    }

    private void checkEmailCode(String email, String code, Long userId) {
        Long check = codeUtils.verify(EMAIL_CODE + email, code);
        if (check.equals(CodeUtils.NULL)) {
            throw new UserException(INVALID_EMAIL_CODE, "UserId=" + userId);
        } else if (check.equals(CodeUtils.FALSE)) {
            throw new UserException(ERROR_EMAIL_CODE, "UserId=" + userId);
        }
    }

    private void checkEmail(String email, Long userId) {
        if (StrUtil.isBlank(email)) {
            throw new UserException(NULL_USER_BIND_EMAIL);
        } else if (!Validators.isEmail(email)) {
            throw new UserException(INVALID_USER_BIND_EMAIL, "UserId=" + userId);
        }
    }

    private void insertAuthEmail(Long userId, String maskedEmail, String email) {
        String aesCurrentVersion = keyProvider.aesCurrentVersion();
        String hmacCurrentVersion = keyProvider.hmacCurrentVersion();
        SecretKey aesKey = keyProvider.getAesKey(aesCurrentVersion);
        SecretKey hmacKey = keyProvider.getHmacKey(hmacCurrentVersion);

        UserAuthEmail userAuthEmail = UserAuthEmail.builder()
                .userId(userId)
                .emailMasked(maskedEmail)
                .emailCipher(aesGcmCrypto.encrypt(email, aesKey))
                .emailHash(hmacBlindIndex.compute(email, hmacKey))
                .keyVersion(aesCurrentVersion)
                .createTime(LocalDateTime.now())
                .build();

        try {
            userAuthEmailMapper.insert(userAuthEmail);
        }catch (DuplicateKeyException e) {
            throw new UserException(EMAIL_EXISTED, "UserId=" + userId);
        }catch (Exception e) {
            throw new UserException(BIND_EMAIL_FAILURE, "UserId=" + userId);
        }
    }

    private void deleteAuthEmail(Long userId) {
        userAuthEmailMapper.deleteAuthEmail(userId, LocalDateTime.now());
    }

    @Override
    public void sendPhoneCode(String phone) {
        Long userId = UserContext.getUserId();

        if (StrUtil.isBlank(phone)) {
            throw new UserException(NULL_PHONE);
        } else if (!Validators.isCnMobile(phone)) {
            throw new UserException(INVALID_PHONE, "UserId=" + userId);
        }

        Long send = codeUtils.send(PHONE_CODE + phone, generateCode());
        if (send.equals(CodeUtils.FALSE)) {
            throw new UserException(PHONE_CODE_EXISTED, "UserId=" + userId);
        }
    }

    @Override
    public void sendEmailCode(String email) {
        Long userId = UserContext.getUserId();

        if (StrUtil.isBlank(email)) {
            throw new UserException(NULL_EMAIL);
        } else if (!Validators.isEmail(email)) {
            throw new UserException(INVALID_EMAIL, "UserId=" + userId);
        }

        Long send = codeUtils.send(EMAIL_CODE + email, generateCode());
        if (send.equals(CodeUtils.FALSE)) {
            throw new UserException(EMAIL_CODE_EXISTED, "UserId=" + userId);
        }
    }

    private String generateCode() {
        return RandomUtil.randomNumbers(6);
    }
}




