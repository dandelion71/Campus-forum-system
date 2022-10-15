package com.dandelion.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.mapper.CommentMapper;
import com.dandelion.system.service.CommentService;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
}
