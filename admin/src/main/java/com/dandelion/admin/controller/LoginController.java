package com.dandelion.admin.controller;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dandelion.common.utils.JwtUtil;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.common.utils.ip.IpUtils;
import com.dandelion.system.dao.LoginBody;
import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@RestController
public class LoginController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @ApiOperation(value = "用户登录")
    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody LoginBody loginBody, HttpServletRequest request) {
        validateCaptcha(loginBody.getCode(),loginBody.getUuid());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        loginBody.getUserName(),
                        loginBody.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("登录失败");
        }
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String jwt = JwtUtil.createJWT(loginUser.getUuid());
        Map<Object, Object> map = new HashMap<>();
        Long id = loginUser.getUser().getId();
        map.put("token", jwt);
        map.put("roleKey", loginUser.getRoleKey());
        map.put("user",loginUser.getUser());
        redisCache.setCacheObject(loginUser.getUuid(), loginUser,7, TimeUnit.DAYS);
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getUserName, loginBody.getUserName())
                .set(User::getLoginDate, new Date())
                .set(User::getLoginIp, IpUtils.getIpAddr(request)));
        return ResponseResult.success(map);
    }

    @ApiOperation(value = "退出")
    @GetMapping("/user/logout")
    public ResponseResult logout() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        redisCache.deleteObject(loginUser.getUuid());
        return ResponseResult.success();
    }

    /**
     * 校验验证码
     */
    public void validateCaptcha(String code, String uuid) {
        String verifyKey = "captcha_codes" + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        Assert.notNull(captcha,"验证码错误");
        if (!code.equalsIgnoreCase(captcha)) {
            throw new IllegalArgumentException("验证码错误");
        }
    }
}
