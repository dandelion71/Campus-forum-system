package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
