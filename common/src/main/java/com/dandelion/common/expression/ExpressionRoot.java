package com.dandelion.common.expression;

import com.dandelion.system.dao.LoginUser;
import com.dandelion.system.mapper.MenuMapper;
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
    private MenuMapper menuMapper;
    public boolean hasAuthority(String authority){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if (menuMapper.selectDataScopeById(loginUser.getUser().getId()).equals("1")){
            return true;
        }
        List<String> permissions = loginUser.getPermissions();
        return permissions.contains(authority);
    }
}
