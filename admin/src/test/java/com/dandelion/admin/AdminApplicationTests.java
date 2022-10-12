package com.dandelion.admin;

import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        System.out.println(SecurityUtils.matchesPassword("1234","$2a$10$VSy/e5iTiTcBGcsUKbmLje12xANdSkEJxgag6p8wJof/nYBVnbtYO"));
    }

}
