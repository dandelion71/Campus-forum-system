package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.User;
import com.dandelion.system.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    String getPassword(String id);

    String getRoleKey(Long id);

    String getIdByUserName(String userName);

    UserVo getUserVoById(Long id);

    void setRole(Long userId,Long roleId);
}
