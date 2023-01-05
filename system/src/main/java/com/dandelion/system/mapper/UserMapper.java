package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.system.dao.User;
import com.dandelion.system.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    PostUserVo getPostUserById(String userId);

    List<PostsSimpleVo> selectUserPost(String userId);

    Long selectUserCollectionPost(String userId);

    IPage<UserDynamic> selectUserDynamic(Page<UserDynamic> page, String userId);

    List<UserVo> getUserByKeyword(String value);
}
