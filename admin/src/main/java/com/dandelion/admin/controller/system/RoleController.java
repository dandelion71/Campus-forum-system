package com.dandelion.admin.controller.system;

import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.RoleMapper;
import com.dandelion.system.service.RoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @ApiOperation(value = "用户角色修改",notes = "根据 id 修改用户角色")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit/userRole/{userId}/{roleId}")
    @PreAuthorize("@dandelion.hasAuthority('system:role:edit')")
    public ResponseResult editUserRole(@PathVariable Long userId, @PathVariable Long roleId){
        roleMapper.updateRoleByUserId(userId, roleId);
        return ResponseResult.success(Massage.UPDATE.value());
    }
}
