package com.dandelion.common.expression;

import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.mapper.MenuMapper;
import com.dandelion.system.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义SpringSecurity权限校验
 */
@Component("dandelion")
public class ExpressionRoot {
    @Autowired
    private UserMapper userMapper;
    public boolean hasAuthority(String authority){
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (SecurityUtils.isAdmin(userMapper.getRoleId(loginUser.getUser().getId()))){
            return true;
        }
        List<String> permissions = loginUser.getPermissions();
        return permissions.contains(authority);
    }
}
