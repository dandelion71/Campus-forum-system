package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Role;
import com.dandelion.system.mapper.RoleMapper;
import com.dandelion.system.service.MenuService;
import com.dandelion.system.service.RoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/permission")
public class PermissionController {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MenuService menuService;


    @ApiOperation(value = "权限管理")
    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:permission:list')")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Role> rolePage = new Page<>(currentPage, pageSize);
        IPage<Role> page = roleService.page(rolePage);
        return ResponseResult.success(page,Massage.SELECT.value());
    }

//    @ApiOperation(value = "权限查询",notes = "根据 roleId 查询拥有权限")
    @GetMapping("/query/have/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:permission:query')")
    public ResponseResult queryHaveByRoleId(@PathVariable String roleId){
        if(SecurityUtils.isAdmin(Long.valueOf(roleId))){
            return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().ne(Menu::getParentId,0)),Massage.SELECT.value());
        }
        return ResponseResult.success(roleMapper.selectHavePermissionByRoleId(roleId),Massage.SELECT.value());
    }

//    @ApiOperation(value = "权限查询",notes = "根据 roleId 查询未拥有权限")
    @GetMapping("/query/none/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:permission:query')")
    public ResponseResult queryNoneByRoleId(@PathVariable String roleId){
        if(SecurityUtils.isAdmin(Long.valueOf(roleId))){
            return ResponseResult.success(Massage.SELECT.value());
        }
        return ResponseResult.success(roleMapper.selectNonePermissionByRoleId(roleId),Massage.SELECT.value());
    }

//    @ApiOperation(value = "权限增加",notes = "根据 roleId menuId 添加权限")
    @Log(title = "权限管理",businessType = BusinessType.INSERT)
    @PostMapping("/query/{roleId}/{menuId}")
    @PreAuthorize("@dandelion.hasAuthority('system:permission:add')")
    public ResponseResult add(@PathVariable String roleId, @PathVariable String menuId){
       roleMapper.insertRoleMenuById(roleId,menuId);
       return ResponseResult.success(Massage.SAVE.value());
    }

//    @ApiOperation(value = "权限删除",notes = "根据 roleId menuId 删除权限")
    @Log(title = "权限管理",businessType = BusinessType.DELETE)
    @PostMapping("/remove/{roleId}/{menuId}")
    @PreAuthorize("@dandelion.hasAuthority('system:permission:remove')")
    public ResponseResult remove(@PathVariable String roleId, @PathVariable String menuId){
        if(SecurityUtils.isAdmin(Long.valueOf(roleId))){
            return ResponseResult.success("该角色为管理员，删除权限失败");
        }
        roleMapper.delRoleMenuByRoleIdAndMenuId(roleId, menuId);
        return ResponseResult.success(Massage.DELETE.value());
    }
}
