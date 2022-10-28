package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.User;
import com.dandelion.system.vo.RoleVo;
import com.dandelion.system.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    String getPassword(String id);

    String getRoleKey(Long id);

    Long getRoleId(Long userId);

    String getIdByUserName(String userName);

    UserVo getUserVoById(Long id);

    RoleVo getRole(Long id);

    List<UserVo> getUsersByQueryString(String queryString);

    void setRole(Long userId,Long roleId);
}
