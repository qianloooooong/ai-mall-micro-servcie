package com.ai.user.utils;

import com.ai.common.exception.IdWorkerException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.ai.common.constants.RedisConstants.ID_INCR;
import static com.ai.common.constants.RedisConstants.ID_INCR_TTL;
import static com.ai.common.enums.StatusCode.ID_WORKER_ERROR;

@Component
@RequiredArgsConstructor
public class RedisIdWorker {

    private final StringRedisTemplate redisTemplate;

    // 2026-01-01 00:00 UTC+8
    private static final long BEGIN_TIMESTAMP = 1767196800L;

    private static final long COUNT_BITS = 32;

    private static final long SECONDS = ID_INCR_TTL.getSeconds();

    private static final DefaultRedisScript<Long> INCREASE_ID_SCRIPT;

    static {
        INCREASE_ID_SCRIPT = new DefaultRedisScript<>();
        INCREASE_ID_SCRIPT.setResultType(Long.class);
        INCREASE_ID_SCRIPT.setLocation(new ClassPathResource("/lua/IncreaseId.lua"));
    }

    public long nextId(String keyPrefix){
        LocalDateTime now = LocalDateTime.now();
        // UTC+8
        long nowSecond = now.toEpochSecond(ZoneOffset.of("+8"));
        long timestamp = nowSecond - BEGIN_TIMESTAMP;

        String date = now.format(DateTimeFormatter.ofPattern(":yyyyMMdd"));

        long count = 0;

        try {
            count = redisTemplate.execute(
                    INCREASE_ID_SCRIPT,
                    List.of(ID_INCR + keyPrefix + date),
                    String.valueOf(SECONDS)
            );
        } catch (Exception e) {
            throw new IdWorkerException(ID_WORKER_ERROR, "获取id失败", e);
        }

        if (count <= 0) {
            throw new IdWorkerException(ID_WORKER_ERROR);
        }

        return timestamp << COUNT_BITS | count;
    }
}