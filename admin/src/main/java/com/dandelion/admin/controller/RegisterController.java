package com.dandelion.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.config.SecurityConfig;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Objects;

@RestController
public class RegisterController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisCache redisCache;

    @ApiOperation(value = "用户注册")
    @PostMapping("/user/register")
    public ResponseResult register(@RequestBody User user) {
        if(Objects.nonNull(user.getUserName())){
            Assert.isNull(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, user.getUserName())), "用户名名已存在");
        }
        if(Objects.nonNull(user.getPhonenumber())){
            Assert.isNull(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getPhonenumber, user.getPhonenumber())), "手机号已绑定其他用户");
        }
        user.setCreateTime(new Date());
        user.setPassword(new SecurityConfig().passwordEncoder().encode(user.getPassword()));
        userService.save(user);
        userMapper.setRole(user.getId(),3L);
        redisCache.deleteObject("topNums");
        return ResponseResult.success("注册成功");
    }
}
