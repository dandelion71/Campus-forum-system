package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Muted;
import com.dandelion.system.mapper.MutedMapper;
import com.dandelion.system.service.MutedService;
import org.springframework.stereotype.Service;

@Service
public class MutedServiceImpl extends ServiceImpl<MutedMapper, Muted> implements MutedService {
}
