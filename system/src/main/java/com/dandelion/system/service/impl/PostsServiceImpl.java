package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.mapper.PostsMapper;
import com.dandelion.system.service.PostsService;
import org.springframework.stereotype.Service;

@Service
public class PostsServiceImpl extends ServiceImpl<PostsMapper, Posts> implements PostsService {
}
