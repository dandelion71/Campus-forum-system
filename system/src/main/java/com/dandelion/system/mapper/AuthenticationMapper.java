package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Authentication;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthenticationMapper extends BaseMapper<Authentication> {
}
