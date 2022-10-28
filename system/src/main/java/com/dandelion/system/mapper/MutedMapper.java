package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.Muted;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MutedMapper extends BaseMapper<Muted> {

    IPage<Muted> getAllByUserName(Page<Muted> page, @Param(Constants.WRAPPER) Wrapper<Muted> queryWrapper, String userName);
}
