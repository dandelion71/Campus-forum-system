package com.dandelion.admin;

import com.dandelion.common.utils.RedisCache;
import com.dandelion.system.service.MutedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class AdminApplicationTests {
    @Autowired
    private RedisCache redisCache;
    @Test
    void redisTest() {
        long keyExpire = redisCache.getKeyExpire("b5ba2b84-7840-4513-ad76-1f25c5bc3e76", TimeUnit.DAYS);
        System.out.println(keyExpire);
    }
}
