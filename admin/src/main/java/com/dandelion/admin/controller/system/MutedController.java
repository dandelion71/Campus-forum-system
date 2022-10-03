package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Muted;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.MutedMapper;
import com.dandelion.system.service.MutedService;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
@RequestMapping("/system/muted")
public class MutedController {

    @Autowired
    private MutedService mutedService;

    @Autowired
    private MutedMapper mutedMapper;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "禁言用户查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:list')")
    @GetMapping("/list")
    public ResponseResult list() {
        return ResponseResult.success(mutedService.list(), Massage.SELECT.value());
    }

    @ApiOperation(value = "禁言用户查询",notes = "根据 用户名 查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byUserName/{userName}")
    public ResponseResult queryByUserName(@PathVariable String userName) {
        return ResponseResult.success(mutedMapper.getAllByUserName(userName), Massage.SELECT.value());
    }

    @ApiOperation(value = "禁言用户查询",notes = "根据 userId 查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byId/{userId}")
    public ResponseResult queryById(@PathVariable String userId) {
        return ResponseResult.success(mutedService.list(new LambdaQueryWrapper<Muted>().eq(Muted::getUserId,userId)), Massage.SELECT.value());
    }

    @ApiOperation(value = "添加用户禁言",notes = "根据 userId 添加")
    @Log(title = "封禁管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:add')")
    @GetMapping("/add/byUserId/{userId}/{day}")
    public ResponseResult addByUserId(@PathVariable String userId,@PathVariable Integer day){
        Muted muted = new Muted();
        muted.setUserId(Long.valueOf(userId));
        muted.setCreateBy(SecurityUtils.getUsername());
        muted.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,day);
        muted.setMutedTime(calendar.getTime());
        mutedService.save(muted);
        return ResponseResult.success("禁言成功!");
    }

}
