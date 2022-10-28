package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Authentication;
import com.dandelion.system.mapper.AuthenticationMapper;
import com.dandelion.system.service.AuthenticationService;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl extends ServiceImpl<AuthenticationMapper, Authentication> implements AuthenticationService {
}
