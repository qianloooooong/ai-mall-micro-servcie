package com.ai.user.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CodeUtils {

    public static final Long NULL = -1L;
    public static final Long TRUE = 1L;
    public static final Long FALSE = 0L;

    private final StringRedisTemplate redisTemplate;

    private static final DefaultRedisScript<Long> VERIFY_CODE_SCRIPT;
    private static final DefaultRedisScript<Long> SEND_CODE_SCRIPT;

    static {
        VERIFY_CODE_SCRIPT = new DefaultRedisScript<>();
        VERIFY_CODE_SCRIPT.setResultType(Long.class);
        VERIFY_CODE_SCRIPT.setLocation(new ClassPathResource("lua/CheckCode.lua"));

        SEND_CODE_SCRIPT = new DefaultRedisScript<>();
        SEND_CODE_SCRIPT.setResultType(Long.class);
        SEND_CODE_SCRIPT.setLocation(new ClassPathResource("lua/SendCode.lua"));
    }

    public Long verify(String key, String code) {
        return redisTemplate.execute(VERIFY_CODE_SCRIPT, List.of(key), code);
    }

    public Long send(String key, String code) {
        return redisTemplate.execute(SEND_CODE_SCRIPT, List.of(key), code, String.valueOf(Duration.ofMinutes(5).getSeconds()));
    }
}
