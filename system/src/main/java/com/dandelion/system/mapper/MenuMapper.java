package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.dandelion.system.dao.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    List<String> selectPermsById(Long id);

    String selectDataScopeById(Long id);

}
