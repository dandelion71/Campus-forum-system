package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    List<Long> selectRoleUserIdByRoleId(String roleId);

    List<Menu> selectHavePermissionByRoleId(String roleId);

    List<Menu> selectNonePermissionByRoleId(String roleId);

    void updateRoleByUserId(Long userId, Long roleId);

    void updateUserRole(String oleRoleId, String newRoleId);

    void insertRoleMenuById(String roleId,String menuId);

    void delRoleUserById(String roleId);

    void delRoleMenuById(String roleId);

    void delRoleMenuByRoleIdAndMenuId(String roleId,String menuId);
}
