package com.ai.user.config;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.ai.common.constants.RedisConstants.*;

@Configuration
@RequiredArgsConstructor
public class BloomFilterConfig {

    private final RedissonClient redissonClient;

    private static final int CAPACITY = 1000000;
    private static final double FALSE_PROBABILITY = 0.03;

    @Bean
    public RBloomFilter<String> usernameBloomFilter() {
        RBloomFilter<String> filter = redissonClient.getBloomFilter(USERNAME_BLOOM_FILTER);
        filter.tryInit(CAPACITY, FALSE_PROBABILITY);
        return filter;
    }

    @Bean
    public RBloomFilter<byte[]> mobileBloomFilter() {
        RBloomFilter<byte[]> filter = redissonClient.getBloomFilter(MOBILE_BLOOM_FILTER);
        filter.tryInit(CAPACITY, FALSE_PROBABILITY);
        return filter;
    }

    @Bean
    public RBloomFilter<byte[]> emailBloomFilter() {
        RBloomFilter<byte[]> filter = redissonClient.getBloomFilter(EMAIL_BLOOM_FILTER);
        filter.tryInit(CAPACITY, FALSE_PROBABILITY);
        return filter;
    }
}