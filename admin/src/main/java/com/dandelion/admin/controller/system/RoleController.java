package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Role;
import com.dandelion.system.mapper.RoleMapper;
import com.dandelion.system.service.RoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @ApiOperation(value = "角色管理")
    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:role:list')")
    public ResponseResult list(){
        return ResponseResult.success(roleService.list(),Massage.SELECT.value());
    }

    @ApiOperation(value = "角色名是否存在",notes = "根据 roleName 查询")
    @GetMapping("/query/roleNameExists/{roleName}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:add')")
    public ResponseResult roleNameExists(@PathVariable String roleName){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName));
        Assert.isNull(role, "角色名已存在");
        return ResponseResult.success("角色名可以使用");
    }

    @ApiOperation(value = "角色权限字符串是否存在",notes = "根据 roleKey 查询")
    @GetMapping("/query/roleKeyExists/{roleKey}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:add')")
    public ResponseResult roleKeyExists(@PathVariable String roleKey){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, roleKey));
        Assert.isNull(role, "角色权限字符串已存在");
        return ResponseResult.success("角色权限字符串可以使用");
    }

    @ApiOperation(value = "角色添加")
    @Log(title = "角色管理",businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@dandelion.hasAuthority('system:role:add')")
    public ResponseResult add(@RequestBody Role role){
        role.setCreateBy(SecurityUtils.getUsername());
        role.setCreateTime(new Date());
        roleService.save(role);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @ApiOperation(value = "角色修改",notes = "根据 roleId 修改角色")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:role:edit')")
    public ResponseResult editRole(@RequestBody Role role){
        role.setUpdateBy(SecurityUtils.getUsername());
        role.setUpdateTime(new Date());
        roleService.updateById(role);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "用户角色修改",notes = "根据 userId roleId 修改用户角色")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit/{userId}/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:edit')")
    public ResponseResult editUserRole(@PathVariable Long userId, @PathVariable Long roleId){
        roleMapper.updateRoleByUserId(userId, roleId);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "角色删除",notes = "根据 id 删除角色")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:remove')")
    public ResponseResult remove(@PathVariable String roleId){
        Role role = roleService.getById(roleId);
        if("1".equals(role.getIsDel())){
            return ResponseResult.fail("该角色不可删除");
        }
        List<Long> userIds = roleMapper.selectRoleUserIdByRoleId(roleId);
        //将删除的角色的用户设置为默认角色
        for (Long userId : userIds) {
            roleMapper.updateRoleByUserId(userId,3L);
        }
        roleMapper.delRoleMenuById(roleId);
        roleMapper.delRoleUserById(roleId);
        roleService.removeById(roleId);
        return ResponseResult.success(Massage.DELETE.value());
    }
}
