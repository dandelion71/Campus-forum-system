package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/system/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:user:list')")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<User> userPage = new Page<>(currentPage, pageSize);
        IPage<User> page = userService.page(
                userPage,
                new LambdaQueryWrapper<User>()
                        .ne(User::getDelFlag, 2)
                        .orderByDesc(User::getLoginDate));
        List<User> users = userPage.getRecords();
        for (User user : users) {
            user.setRole(userMapper.getRole(user.getId()));
        }
        return ResponseResult.success(page);
    }

    @GetMapping("/query/byId/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:user:query')")
    public ResponseResult getOneById(@PathVariable String id) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .ne(User::getDelFlag, 2)
                .eq(User::getId, id));
        user.setRole(userMapper.getRole(user.getId()));
        return ResponseResult.success(user);
    }

    @GetMapping("/query/byUserName/{userName}")
    @PreAuthorize("@dandelion.hasAuthority('system:user:query')")
    public ResponseResult getOneByUserName(@PathVariable String userName) {
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .ne(User::getDelFlag, 2)
                .eq(User::getUserName, userName));
        Assert.notNull(user,"未查询到该用户");
        user.setRole(userMapper.getRole(user.getId()));
        return ResponseResult.success(user,Massage.SELECT.value());
    }

    @GetMapping("/queryUser/byUserName/{queryString}")
    @PreAuthorize("@dandelion.hasAuthority('system:user:query')")
    public ResponseResult queryUser(@PathVariable String queryString) {
        return ResponseResult.success(userMapper.getUsersByQueryString(queryString),Massage.SELECT.value());
    }

    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:user:edit')")
    public ResponseResult edit(@RequestBody User user) {
        if(Objects.nonNull(user.getUserName())){
            Assert.isNull(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName, user.getUserName())), "用户名已存在");
        }
        if(Objects.nonNull(user.getPhonenumber())){
            Assert.isNull(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getPhonenumber, user.getPhonenumber())), "手机号已绑定其他用户");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        user.setUpdateTime(new Date());
        userService.updateById(user);
        return ResponseResult.success(Massage.UPDATE.value());
    }
}
