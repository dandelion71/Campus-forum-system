package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Role;
import com.dandelion.system.mapper.RoleMapper;
import com.dandelion.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/system/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:role:list')")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Role> rolePage = new Page<>(currentPage, pageSize);
        IPage<Role> page = roleService.page(rolePage);
        return ResponseResult.success(page);
    }

    @GetMapping("/queryAll")
    @PreAuthorize("@dandelion.hasAuthority('system:role:query')")
    public ResponseResult queryAll(){
        return ResponseResult.success(roleMapper.getAll());
    }

    @GetMapping("/query/roleById/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:query')")
    public ResponseResult roleById(@PathVariable String roleId){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getId, roleId));
        Assert.notNull(role, "未找到该角色");
        return ResponseResult.success(role,Massage.SELECT.value());
    }

    @GetMapping("/query/roleByRoleName/{roleName}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:query')")
    public ResponseResult roleByRoleName(@PathVariable String roleName){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName));
        Assert.notNull(role, "未找到该角色");
        return ResponseResult.success(role,Massage.SELECT.value());
    }

    @Log(title = "角色管理",businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@dandelion.hasAuthority('system:role:add')")
    public ResponseResult add(@RequestBody Role role){
        queryExists(role);
        role.setCreateBy(SecurityUtils.getUsername());
        role.setCreateTime(new Date());
        roleService.save(role);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:role:edit')")
    public ResponseResult editRole(@RequestBody Role role){
        queryExists(role);
        role.setUpdateBy(SecurityUtils.getUsername());
        role.setUpdateTime(new Date());
        roleService.updateById(role);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{oldRoleId}/{newRoleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:remove')")
    public ResponseResult remove(@PathVariable String oldRoleId, @PathVariable String newRoleId){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getId, oldRoleId).ne(Role::getIsDel, "1"));
        Assert.notNull(role,"该角色不可删除");
        roleMapper.updateUserRole(oldRoleId,newRoleId);
        roleMapper.delRoleMenuById(oldRoleId);
        roleMapper.delRoleUserById(oldRoleId);
        roleService.removeById(oldRoleId);
        return ResponseResult.success(Massage.DELETE.value());
    }

    private void queryExists(Role role){
        if(Objects.nonNull(role.getRoleName())){
            Assert.isNull(roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, role.getRoleName())), "角色名已存在");
        }
        if (Objects.nonNull(role.getRoleKey())){
            Assert.isNull(roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleKey, role.getRoleKey())), "角色权限字符串已存在");
        }
    }
}
