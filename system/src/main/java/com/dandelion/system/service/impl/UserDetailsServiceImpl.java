package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.MenuMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.UserService;
import com.dandelion.system.vo.UserVo;
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
    private UserMapper userMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUserName,loginName).ne(User::getDelFlag,2));
        if (Objects.isNull(user)){
            throw new RuntimeException("用户名或密码错误");
        }
        user.setPassword(userMapper.getPassword(String.valueOf(user.getId())));
        if ("2".equals(user.getMuted())){
            throw new RuntimeException("该用户已被永久封禁");
        }
        UserVo userVo = new UserVo();
        userVo.setId(user.getId());
        userVo.setUserName(user.getUserName());
        userVo.setAvatar(user.getAvatar());
        userVo.setMuted(user.getMuted());
        userVo.setStatus(user.getStatus());
        return new LoginUser(userVo,UUID.randomUUID().toString(),userMapper.getRoleKey(user.getId()), user.getPassword(), menuMapper.selectPermsById(user.getId()));
    }
}
