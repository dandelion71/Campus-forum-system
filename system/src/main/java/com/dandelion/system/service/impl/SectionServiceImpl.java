package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Section;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.SectionService;
import org.springframework.stereotype.Service;

@Service
public class SectionServiceImpl extends ServiceImpl<SectionMapper, Section> implements SectionService {
}
