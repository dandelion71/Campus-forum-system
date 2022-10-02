package com.dandelion.common.handler;

import com.alibaba.fastjson.JSON;
import com.dandelion.common.utils.WebUtils;
import com.dandelion.system.dao.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ResponseResult result = ResponseResult.fail(HttpStatus.UNAUTHORIZED.value(), "用户认证失败，请重新登录");
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
