package com.dandelion.common.handler;

import com.alibaba.fastjson.JSON;
import com.dandelion.common.utils.WebUtils;
import com.dandelion.system.dao.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseResult result = ResponseResult.fail(HttpStatus.FORBIDDEN.value(), "您的权限不足");
        WebUtils.renderString(response, JSON.toJSONString(result));
    }
}
