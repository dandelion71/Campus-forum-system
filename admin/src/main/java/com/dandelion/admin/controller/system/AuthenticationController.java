package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.system.dao.Authentication;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.service.AuthenticationService;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system/authentication")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "认证申请管理")
    @PreAuthorize("@dandelion.hasAuthority('system:authentication:list')")
    @GetMapping("/listNonePass")
    public ResponseResult listNonePass(@RequestParam(defaultValue = "1") Integer currentPage,
                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Authentication> commentPage = new Page<>(currentPage, pageSize);
        IPage<Authentication> page = authenticationService.page(commentPage,
                new LambdaQueryWrapper<Authentication>()
                        .isNull(Authentication::getExpire));
        return ResponseResult.success(page);
    }

    @ApiOperation(value = "已认证管理")
    @PreAuthorize("@dandelion.hasAuthority('system:authentication:list')")
    @GetMapping("/listPass")
    public ResponseResult listPass(@RequestParam(defaultValue = "1") Integer currentPage,
                                    @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Authentication> commentPage = new Page<>(currentPage, pageSize);
        IPage<Authentication> page = authenticationService.page(commentPage,
                new LambdaQueryWrapper<Authentication>()
                        .isNotNull(Authentication::getExpire));
        return ResponseResult.success(page);
    }

    @ApiOperation(value = "认证修改",notes = "认证申请通过")
    @Log(title = "认证管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editPass")
    @PreAuthorize("@dandelion.hasAuthority('system:authentication:edit')")
    public ResponseResult editPass(@RequestBody Authentication authentication){
        userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,authentication.getUserId()).set(User::getStatus,1));
        authenticationService.updateById(authentication);
        return ResponseResult.success("认证成功");
    }

    @ApiOperation(value = "认证修改",notes = "认证申请不通过")
    @Log(title = "认证管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editNonePass")
    @PreAuthorize("@dandelion.hasAuthority('system:authentication:edit')")
    public ResponseResult editNonePass(@RequestBody Authentication authentication){
        authenticationService.updateById(authentication);
        return ResponseResult.success("拒绝成功");
    }

}
