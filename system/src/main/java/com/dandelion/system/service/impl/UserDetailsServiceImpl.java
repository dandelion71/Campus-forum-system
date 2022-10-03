package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.MenuMapper;
import com.dandelion.system.service.MutedService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName,loginName).ne(User::getDelFlag,2));
        if (Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        if ("2".equals(user.getMuted())){
            throw new RuntimeException("该用户已被永久封禁");
        }
        return new LoginUser(user,UUID.randomUUID().toString(),menuMapper.selectPermsById(user.getId()));
    }
}
