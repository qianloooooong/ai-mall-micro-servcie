package com.ai.user.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.api.dto.UserLoginDTO;
import com.ai.common.enums.*;
import com.ai.common.enums.UserStatus;
import com.ai.common.exception.UserException;
import com.ai.common.utils.UserContext;
import com.ai.common.utils.Validators;
import com.ai.user.crypto.AesGcmCrypto;
import com.ai.user.hmac.HmacBlindIndex;
import com.ai.user.keyprovider.KeyProvider;
import com.ai.user.mapper.*;
import com.ai.user.pojo.dto.UserBindEmailDTO;
import com.ai.user.pojo.dto.UserBindPhoneDTO;
import com.ai.user.pojo.dto.UserRegisterDTO;
import com.ai.user.pojo.dto.UserResetPasswordDTO;
import com.ai.user.pojo.entity.*;
import com.ai.user.service.UserAuthCredentialService;
import com.ai.user.service.UserAuthEmailService;
import com.ai.user.service.UserAuthMobileService;
import com.ai.user.service.UserService;
import com.ai.user.utils.CodeUtils;
import com.ai.user.utils.MaskedUtil;
import com.ai.user.utils.RedisIdWorker;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ai.common.constants.RedisConstants.*;
import static com.ai.common.enums.StatusCode.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Autowired
    @Lazy
    private UserService self;

    private final HmacBlindIndex hmacBlindIndex;

    private final AesGcmCrypto aesGcmCrypto;

    private final KeyProvider keyProvider;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    private final UserMapper userMapper;
    
    private final UserRoleMapper userRoleMapper;

    private final UserAuthMobileService userAuthMobileService;

    private final UserAuthMobileMapper userAuthMobileMapper;

    private final UserAuthEmailService userAuthEmailService;

    private final UserAuthEmailMapper userAuthEmailMapper;

    private final UserAuthCredentialService userAuthCredentialService;

    private final UserAuthCredentialMapper userAuthCredentialMapper;

    private final CodeUtils codeUtils;

    private final RedisIdWorker redisIdWorker;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserLoginDTO getUserLoginInfo(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new UserException(NULL_USERNAME);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        UserLoginDTO loginInfo = null;
        String loginType = userLoginDTO.getLoginType();

        if (LoginType.USERNAME_PASSWORD.getType().equals(loginType)) {
            wrapper.eq("u.username", username);
            wrapper.eq("u.status", UserStatus.ENABLE.getValue());
            wrapper.eq("u.deleted", LogicDeleted.NOT_DELETED.getValue());
            wrapper.eq("uac.deleted", LogicDeleted.NOT_DELETED.getValue());

            loginInfo = userMapper.getUserLoginInfoByUsername(wrapper);
        } else if (LoginType.MOBILE_PASSWORD.getType().equals(loginType)) {
            wrapper.eq("uam.mobile_hash", hmacBlindIndex.compute(userLoginDTO.getUsername(), keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())));
            wrapper.eq("u.status", UserStatus.ENABLE.getValue());
            wrapper.eq("u.deleted", LogicDeleted.NOT_DELETED.getValue());
            wrapper.eq("uam.deleted", LogicDeleted.NOT_DELETED.getValue());
            wrapper.eq("uac.deleted", LogicDeleted.NOT_DELETED.getValue());

            loginInfo = userMapper.getUserLoginInfoByMobile(wrapper);
        } else if (LoginType.EMAIL_PASSWORD.getType().equals(loginType)) {
            wrapper.eq("uae.email_hash", hmacBlindIndex.compute(userLoginDTO.getUsername(), keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())));
            wrapper.eq("u.status", UserStatus.ENABLE.getValue());
            wrapper.eq("u.deleted", LogicDeleted.NOT_DELETED.getValue());
            wrapper.eq("uae.deleted", LogicDeleted.NOT_DELETED.getValue());
            wrapper.eq("uac.deleted", LogicDeleted.NOT_DELETED.getValue());

            loginInfo = userMapper.getUserLoginInfoByEmail(wrapper);
        }


        if (loginInfo == null) {
            throw new UserException(UNAUTHORIZED_LOGIN_INFO_ERROR);
        }

        List<String> list = userRoleMapper.getRoles(loginInfo.getId());
        Set<String> roles = new HashSet<>(list);
        loginInfo.setRoles(roles);

        return loginInfo;
    }

    @Override
    public UserLoginDTO loginByCode(UserLoginDTO userLoginDTO) {
        String loginType = userLoginDTO.getLoginType();
        if (!LoginType.isCodeLogin(loginType)) {
            throw new UserException(UNAUTHORIZED_LOGIN_INFO_ERROR);
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("u.status", UserStatus.ENABLE.getValue());
        wrapper.eq("u.deleted", LogicDeleted.NOT_DELETED.getValue());
        UserLoginDTO loginInfo = null;

        RBloomFilter<String> usernameBloomFilter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);

        if (LoginType.MOBILE_CODE.getType().equals(loginType)) {
            String phone = userLoginDTO.getUsername();
            String code = userLoginDTO.getPassword();
            if (Validators.isCnMobile(phone)) {
                RBloomFilter<byte[]> phoneBloomFilter = redissonClient.getBloomFilter(MOBILE_BLOOM_FILTER);

                byte[] phoneHash = hmacBlindIndex.compute(phone, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion()));
                if (!phoneBloomFilter.contains(phoneHash)) {
                    User user = self.register(UserRegisterDTO.builder().phone(phone).phoneCode(code).build());
                    Set<String> roles = Set.of(Roles.USER.getValue());

                    usernameBloomFilter.add(user.getUsername());
                    phoneBloomFilter.add(phoneHash);

                    return UserLoginDTO.builder().id(user.getId()).username(user.getUsername()).roles(roles).build();
                }
                checkPhoneCode(phone, code, null);
            } else {
                throw new UserException(INVALID_PHONE);
            }

            wrapper.eq("uam.mobile_hash", hmacBlindIndex.compute(phone, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())));
            wrapper.eq("uam.deleted", LogicDeleted.NOT_DELETED.getValue());
            loginInfo = userMapper.getUserLoginInfoByMobile(wrapper);
        } else if (LoginType.EMAIL_CODE.getType().equals(loginType)) {
            String email = userLoginDTO.getUsername();
            String code = userLoginDTO.getPassword();
            if (Validators.isEmail(email)) {
                RBloomFilter<byte[]> emailBloomFilter = redissonClient.getBloomFilter(EMAIL_BLOOM_FILTER);

                byte[] emailHash = hmacBlindIndex.compute(email, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion()));
                if (!emailBloomFilter.contains(emailHash)) {
                    User user = self.register(UserRegisterDTO.builder().email(email).emailCode(code).build());
                    Set<String> roles = Set.of(Roles.USER.getValue());

                    usernameBloomFilter.add(user.getUsername());
                    emailBloomFilter.add(emailHash);

                    return UserLoginDTO.builder().id(user.getId()).username(user.getUsername()).roles(roles).build();
                }

                checkEmailCode(email, code, null);
            } else {
                throw new UserException(INVALID_EMAIL);
            }

            wrapper.eq("uae.email_hash", hmacBlindIndex.compute(email, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())));
            wrapper.eq("uae.deleted", LogicDeleted.NOT_DELETED.getValue());
            loginInfo = userMapper.getUserLoginInfoByEmail(wrapper);
        }

        if (loginInfo == null) {
            throw new UserException(UNAUTHORIZED_LOGIN_INFO_ERROR);
        }

        loginInfo.setRoles(new HashSet<>(userRoleMapper.getRoles(loginInfo.getId())));

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
                .eq(UserAuthMobile::getDeleted, LogicDeleted.NOT_DELETED.getValue())
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
                .eq(UserAuthEmail::getDeleted, LogicDeleted.NOT_DELETED.getValue())
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
    public void resetPassword(UserResetPasswordDTO userResetPasswordDTO) {
        Long userId = UserContext.getUserId();
        String phone = userResetPasswordDTO.getPhone();
        String email = userResetPasswordDTO.getEmail();
        String oldPassword = userResetPasswordDTO.getOldPassword();

        if (StrUtil.isNotBlank(oldPassword)) {
            String newPassword = userResetPasswordDTO.getNewPassword();
            String confirmNewPassword = userResetPasswordDTO.getConfirmNewPassword();

            if (!Validators.isPassword(newPassword)) {
                throw new UserException(INVALID_PASSWORD, "UserId=" + userId);
            }

            if (!newPassword.equals(confirmNewPassword)) {
                throw new UserException(CONFIRM_PASSWORD_NOT_MATCH, "UserId=" + userId);
            }

            UserAuthCredential userAuthCredential = userAuthCredentialService.lambdaQuery()
                    .eq(UserAuthCredential::getUserId, userId)
                    .eq(UserAuthCredential::getDeleted, LogicDeleted.NOT_DELETED.getValue())
                    .one();
            String passwordHash = userAuthCredential.getPasswordHash();

            if (!passwordEncoder.matches(oldPassword, passwordHash)) {
                throw new UserException(INVALID_RESET_PASSWORD_INFO, "UserId=" + userId);
            }

            try {
                userAuthCredentialService.lambdaUpdate()
                        .eq(UserAuthCredential::getUserId, userId)
                        .eq(UserAuthCredential::getDeleted, LogicDeleted.NOT_DELETED.getValue())
                        .set(UserAuthCredential::getPasswordHash, passwordEncoder.encode(newPassword))
                        .update();
            } catch (Exception e) {
                throw new UserException(RESET_PASSWORD_FAILURE, "UserId=" + userId);
            }

        } else if (StrUtil.isNotBlank(phone)) {
            String code = userResetPasswordDTO.getPhoneCode();
            checkPhoneCode(phone, code, userId);

            try {
                userAuthCredentialService.lambdaUpdate()
                        .eq(UserAuthCredential::getUserId, userId)
                        .eq(UserAuthCredential::getDeleted, LogicDeleted.NOT_DELETED.getValue())
                        .set(UserAuthCredential::getPasswordHash, passwordEncoder.encode(userResetPasswordDTO.getNewPassword()))
                        .update();
            } catch (Exception e) {
                throw new UserException(RESET_PASSWORD_FAILURE, "UserId=" + userId);
            }
        } else if (StrUtil.isNotBlank(email)) {
            String code = userResetPasswordDTO.getEmailCode();
            checkEmailCode(email, code, userId);

            try {
                userAuthCredentialService.lambdaUpdate()
                        .eq(UserAuthCredential::getUserId, userId)
                        .eq(UserAuthCredential::getDeleted, LogicDeleted.NOT_DELETED.getValue())
                        .set(UserAuthCredential::getPasswordHash, passwordEncoder.encode(userResetPasswordDTO.getNewPassword()))
                        .update();
            } catch (Exception e) {
                throw new UserException(RESET_PASSWORD_FAILURE, "UserId=" + userId);
            }
        } else {
            throw new UserException(INVALID_RESET_PASSWORD_INFO, "UserId=" + userId);
        }
    }

    @Override
    public void resetNickname(String nickname) {
        if (StrUtil.isBlank(nickname)) {
            throw new UserException(NULL_NICKNAME);
        } else if (nickname.length() > 32) {
            throw new UserException(TO_LONG_NICKNAME);
        }
        Long userId = UserContext.getUserId();
        lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getNickname, nickname)
                .eq(User::getDeleted, LogicDeleted.NOT_DELETED.getValue())
                .update();
    }

    @Override
    public void sendPhoneCode(String phone) {
        Long userId = UserContext.getUserId();

        if (StrUtil.isBlank(phone)) {
            throw new UserException(NULL_PHONE);
        } else if (!Validators.isCnMobile(phone)) {
            throw new UserException(INVALID_PHONE, "UserId=" + userId);
        }

        Long send = codeUtils.send(PHONE_CODE, PHONE_CODE_LIMITER, phone, generateCode());
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

        Long send = codeUtils.send(EMAIL_CODE, EMAIL_CODE_LIMITER, email, generateCode());
        if (send.equals(CodeUtils.FALSE)) {
            throw new UserException(EMAIL_CODE_EXISTED, "UserId=" + userId);
        }
    }

    private String generateCode() {
        return "000000"; // TODO Delete
        //return RandomUtil.randomNumbers(6);
    }

    /**
     * 注册：phone 与 email 二选一，业务上不会同时提供。
     * - 有 username：必须提供 phone 或 email 之一，且必须设置 password
     * - 无 username：必须提供 phone 或 email 之一，不设置 password
     */
    @Override
    @Transactional
    public User register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        String phone = userRegisterDTO.getPhone();
        String email = userRegisterDTO.getEmail();

        if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(phone)) {
            return createUserByUsernameAndPhone(userRegisterDTO);
        } else if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(email)) {
            return createUserByUsernameAndEmail(userRegisterDTO);
        } else if (StrUtil.isBlank(username) && StrUtil.isNotBlank(phone)) {
            return createUserByPhone(userRegisterDTO);
        } else if (StrUtil.isBlank(username) && StrUtil.isNotBlank(email)) {
            return createUserByEmail(userRegisterDTO);
        }  else {
            throw new UserException(INVALID_REGISTER_INFO);
        }
    }

    private User createUserByUsernameAndPhone(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();
        if (!Validators.isCnMobile(phone)) {
            throw new UserException(INVALID_PHONE);
        }
        if (!Validators.isUsername(username)) {
            throw new UserException(INVALID_USERNAME);
        }
        if (StrUtil.isBlank(password)) {
            throw new UserException(NULL_PASSWORD);
        }
        if (!Validators.isPassword(password)) {
            throw new UserException(INVALID_PASSWORD);
        }
        if (!password.equals(confirmPassword)) {
            throw new UserException(CONFIRM_PASSWORD_NOT_MATCH);
        }

        String phoneCode = userRegisterDTO.getPhoneCode();
        checkPhoneCode(phone, phoneCode, null);

        LocalDateTime now = LocalDateTime.now();
        long userno = redisIdWorker.nextId("userno");

        User user = insertUser(username, userno, now);

        insertUserCredential(password, user.getId(), now);

        insertUserPhone(phone, user.getId(), now);

        insertUserRole(user);

        return user;
    }

    private User createUserByUsernameAndEmail(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String username = userRegisterDTO.getUsername();
        String password = userRegisterDTO.getPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();
        if (!Validators.isEmail(email)) {
            throw new UserException(INVALID_EMAIL);
        }
        if (!Validators.isUsername(username)) {
            throw new UserException(INVALID_USERNAME);
        }
        if (StrUtil.isBlank(password)) {
            throw new UserException(NULL_PASSWORD);
        }
        if (!Validators.isPassword(password)) {
            throw new UserException(INVALID_PASSWORD);
        }
        if (!password.equals(confirmPassword)) {
            throw new UserException(CONFIRM_PASSWORD_NOT_MATCH);
        }

        String emailCode = userRegisterDTO.getEmailCode();
        checkEmailCode(email, emailCode, null);

        LocalDateTime now = LocalDateTime.now();
        long userno = redisIdWorker.nextId("userno");

        User user = insertUser(username, userno, now);

        insertUserCredential(password, user.getId(), now);

        insertUserEmail(email, user.getId(), now);

        insertUserRole(user);

        return user;
    }

    private User createUserByPhone(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        if (!Validators.isCnMobile(phone)) {
            throw new UserException(INVALID_PHONE);
        }

        String phoneCode = userRegisterDTO.getPhoneCode();
        checkPhoneCode(phone, phoneCode, null);

        LocalDateTime now = LocalDateTime.now();
        long userno = redisIdWorker.nextId("userno");

        User user = insertUser(defaultUsername(userno), userno, now);

        insertUserPhone(phone, user.getId(), now);

        insertUserRole(user);

        return user;
    }

    private User createUserByEmail(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        if (!Validators.isEmail(email)) {
            throw new UserException(INVALID_EMAIL);
        }

        String emailCode = userRegisterDTO.getEmailCode();
        checkEmailCode(email, emailCode, null);

        LocalDateTime now = LocalDateTime.now();
        long userno = redisIdWorker.nextId("userno");

        User user = insertUser(defaultUsername(userno), userno, now);

        insertUserEmail(email, user.getId(), now);

        insertUserRole(user);

        return user;
    }

    private User insertUser(String username, long userno, LocalDateTime now) {
        User user = User.builder()
                .userNo(userno)
                .username(username)
                .nickname("nick-" + RandomUtil.randomNumbers(10))
                .avatar(null)
                .gender(Genders.NON_SETTING.getValue())
                .createTime(now)
                .updateTime(now)
                .status(UserStatus.ENABLE.getValue())
                .deleted(LogicDeleted.NOT_DELETED.getValue())
                .deleteTime(null)
                .build();
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            throw new UserException(USERNAME_EXISTED);
        } catch (Exception e) {
            throw new UserException(INSERT_USER_FAILURE);
        }
        return user;
    }

    private void insertUserCredential(String password, Long id, LocalDateTime now) {
        UserAuthCredential userAuthCredential = UserAuthCredential.builder()
                .userId(id)
                .passwordHash(passwordEncoder.encode(password))
                .createTime(now)
                .updateTime(now)
                .deleted(LogicDeleted.NOT_DELETED.getValue())
                .deleteTime(null)
                .build();
        try {
            userAuthCredentialMapper.insert(userAuthCredential);
        } catch (Exception e) {
            throw new UserException(INSERT_USER_CREDENTIAL_FAILURE);
        }
    }

    private void insertUserPhone(String phone, Long id, LocalDateTime now) {
        UserAuthMobile userAuthMobile = UserAuthMobile.builder()
                .userId(id)
                .mobileCipher(aesGcmCrypto.encrypt(phone, keyProvider.getAesKey(keyProvider.aesCurrentVersion())))
                .mobileHash(hmacBlindIndex.compute(phone, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())))
                .keyVersion(keyProvider.aesCurrentVersion())
                .mobileMasked(MaskedUtil.maskedPhone(phone))
                .createTime(now)
                .deleted(LogicDeleted.NOT_DELETED.getValue())
                .deleteTime(null)
                .build();
        try {
            userAuthMobileMapper.insert(userAuthMobile);
        } catch (DuplicateKeyException e) {
            throw new UserException(PHONE_EXISTED);
        } catch (Exception e) {
            throw new UserException(INSERT_USER_PHONE_FAILURE);
        }
    }

    private void insertUserEmail(String email, Long id, LocalDateTime now) {
        UserAuthEmail userAuthEmail = UserAuthEmail.builder()
                .userId(id)
                .emailCipher(aesGcmCrypto.encrypt(email, keyProvider.getAesKey(keyProvider.aesCurrentVersion())))
                .emailHash(hmacBlindIndex.compute(email, keyProvider.getHmacKey(keyProvider.hmacCurrentVersion())))
                .keyVersion(keyProvider.aesCurrentVersion())
                .emailMasked(MaskedUtil.maskedEmail(email))
                .createTime(now)
                .deleted(LogicDeleted.NOT_DELETED.getValue())
                .deleteTime(null)
                .build();
        try {
            userAuthEmailMapper.insert(userAuthEmail);
        } catch (DuplicateKeyException e) {
            throw new UserException(EMAIL_EXISTED);
        } catch (Exception e) {
            throw new UserException(INSERT_USER_EMAIL_FAILURE);
        }
    }

    private void insertUserRole(User user) {
        UserRole userRole = UserRole.builder()
                .userId(user.getId())
                .roleId(Roles.USER.getCode())
                .build();
        try {
            userRoleMapper.insert(userRole);
        } catch (Exception e) {
            throw new UserException(INSERT_USER_ROLE_FAILURE);
        }
    }

    private String defaultUsername(long userno) {
        return "user" + userno;
    }

    @Override
    public void checkUsername(String username) {
        RBloomFilter<String> usernameBloomFilter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);
        if (usernameBloomFilter.contains(username)) {
            throw new UserException(USERNAME_EXISTED);
        }
    }
}




