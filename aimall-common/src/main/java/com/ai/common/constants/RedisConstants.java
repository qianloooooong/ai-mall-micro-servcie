package com.ai.common.constants;

import java.time.Duration;

public interface RedisConstants {
    String PHONE_CODE = "am:phone:code:";
    String EMAIL_CODE = "am:email:code:";
    Duration CODE_TTL = Duration.ofMinutes(10);
    String PHONE_CODE_LIMITER = "am:phone:code:limit:";
    String EMAIL_CODE_LIMITER = "am:email:code:limit:";
    Duration CODE_LIMITER_TTL = Duration.ofMinutes(1);
}
