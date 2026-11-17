package com.ai.user.init;

import com.ai.user.pojo.entity.User;
import com.ai.user.pojo.entity.UserAuthEmail;
import com.ai.user.pojo.entity.UserAuthMobile;
import com.ai.user.service.UserAuthEmailService;
import com.ai.user.service.UserAuthMobileService;
import com.ai.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterWarmUpRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 5000;

    private final UserService userService;

    private final UserAuthMobileService userAuthMobileService;

    private final UserAuthEmailService userAuthEmailService;

    private final RBloomFilter<String> userBloomFilter;

    private final RBloomFilter<byte[]> mobileBloomFilter;

    private final RBloomFilter<byte[]> emailBloomFilter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        warmUpUsers();
        warmUpUserAuthMobile();
        warmUpUserAuthEmail();
    }

    private void warmUpUsers() {
        long total = 0;
        long lastId = 0;

        while (true) {
            List<User> batch = userService.lambdaQuery()
                    .gt(User::getId, lastId)
                    .last("limit " + BATCH_SIZE)
                    .list();
            if (batch.isEmpty()) {
                break;
            }
            lastId = batch.get(batch.size() - 1).getId();
            total += batch.size();
            List<String> list = batch.stream().map(User::getUsername).toList();
            userBloomFilter.add(list);
        }

        log.info("布隆过滤器userBloomFilter预热完成，共{}个元素", total);
    }

    private void warmUpUserAuthMobile() {
        long total = 0;
        long lastId = 0;

        while (true) {
            List<UserAuthMobile> batch = userAuthMobileService.lambdaQuery()
                    .gt(UserAuthMobile::getId, lastId)
                    .last("limit " + BATCH_SIZE)
                    .list();
            if (batch.isEmpty()) {
                break;
            }
            lastId = batch.get(batch.size() - 1).getId();
            total += batch.size();
            List<byte[]> list = batch.stream().map(UserAuthMobile::getMobileHash).toList();
            mobileBloomFilter.add(list);
        }

        log.info("布隆过滤器mobileBloomFilter预热完成，共{}个元素", total);
    }

    private void warmUpUserAuthEmail() {
        long total = 0;
        long lastId = 0;

        while (true) {
            List<UserAuthEmail> batch = userAuthEmailService.lambdaQuery()
                    .gt(UserAuthEmail::getId, lastId)
                    .last("limit " + BATCH_SIZE)
                    .list();
            if (batch.isEmpty()) {
                break;
            }
            lastId = batch.get(batch.size() - 1).getId();
            total += batch.size();
            List<byte[]> list = batch.stream().map(UserAuthEmail::getEmailHash).toList();
            emailBloomFilter.add(list);
        }

        log.info("布隆过滤器emailBloomFilter预热完成，共{}个元素", total);
    }
}
