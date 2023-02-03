package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Authentication;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.AuthenticationService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/system/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PreAuthorize("@dandelion.hasAuthority('system:authentication:list')")
    @GetMapping("/list")
    public ResponseResult queryAuth(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "5") Integer pageSize,
                                       @RequestParam(defaultValue = "0") String key,
                                       @RequestParam(defaultValue = "0") String value) {
        LambdaQueryWrapper<Authentication> queryWrapper = new LambdaQueryWrapper<>();
        switch (key){
            case "0":break;
            case "1":queryWrapper.eq(Authentication::getPass,1);break;
            case "2":queryWrapper.le(Authentication::getExpire,new Date());break;
            case "3":queryWrapper.eq(Authentication::getUserId,value);break;
        }
        Page<Authentication> authenticationPage = new Page<>(currentPage, pageSize);
        IPage<Authentication> page = authenticationService.page(
                authenticationPage,
                queryWrapper.orderByDesc(Authentication::getCreateTime));
        for (Authentication authentication : page.getRecords()) {
            authentication.setUser(userMapper.getUserVoById(authentication.getUserId()));
        }
        return ResponseResult.success(page);
    }

    @Log(title = "认证管理",businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:authentication:edit')")
    public ResponseResult editPass(@RequestBody Authentication authentication){
        userService.update(
                new LambdaUpdateWrapper<User>()
                        .eq(User::getId,authentication.getUserId())
                        .set(User::getStatus,authentication.getStatus()));
        authentication.setStatus(null);
        authentication.setUpdateBy(SecurityUtils.getUsername());
        authentication.setUpdateTime(new Date());
        authenticationService.updateById(authentication);
        return ResponseResult.success("");
    }
}
