package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Tag;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {
}
