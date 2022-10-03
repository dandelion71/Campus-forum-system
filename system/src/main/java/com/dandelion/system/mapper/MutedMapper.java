package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Muted;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MutedMapper extends BaseMapper<Muted> {

    List<Muted> getAllByUserName(String userName);
}
