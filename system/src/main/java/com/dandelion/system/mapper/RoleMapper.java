package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.Role;
import com.dandelion.system.vo.MenuVo;
import com.dandelion.system.vo.RoleVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Long> selectRoleUserIdByRoleId(String roleId);

    IPage<MenuVo> selectHavePermissionByRoleId(Page<MenuVo> page, @Param(Constants.WRAPPER) Wrapper<Menu> queryWrapper, String roleId);

    List<MenuVo> selectNonePermissionByRoleId(String roleId);

    List<RoleVo> getAll();

    void updateRoleByUserId(Long userId, Long roleId);

    void updateUserRole(String oldRoleId, String newRoleId);

    void insertRoleMenuById(String roleId,String menuId);

    void delRoleUserById(String roleId);

    void delRoleMenuById(String roleId);

    void delRoleMenuByRoleIdAndMenuId(String roleId,String menuId);
}
