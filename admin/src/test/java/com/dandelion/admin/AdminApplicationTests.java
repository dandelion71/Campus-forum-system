package com.dandelion.admin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.dao.Muted;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.MutedService;
import com.dandelion.system.vo.PostsSimpleVo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class AdminApplicationTests {
    @Autowired
    private MutedService mutedService;
    @Test
    void redisTest() {
    }
}
