package com.dandelion.admin.controller;

import com.dandelion.common.annotation.Log;
import com.dandelion.common.config.SecurityConfig;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class RegisterController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @ApiOperation(value = "用户注册")
    @PostMapping("/user/register")
    public ResponseResult register(@RequestBody User user) {
        user.setCreateTime(new Date());
        user.setPassword(new SecurityConfig().passwordEncoder().encode(user.getPassword()));
        userService.save(user);
        userMapper.setRole(user.getId(),3L);
        return ResponseResult.success(HttpStatus.OK.value(), "注册成功");
    }
}
