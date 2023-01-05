package com.dandelion.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
class AdminApplicationTests {
    @Autowired
    private RedisCache redisCache;
    @Test
    void redisTest(){
        Map<String, IPage<Comment>> map = redisCache.getCacheMap("queryCommentPage-"+3+"-"+1);
        IPage<Comment> page = map.get("page");
        page.getRecords().forEach(System.out::println);
    }
}
