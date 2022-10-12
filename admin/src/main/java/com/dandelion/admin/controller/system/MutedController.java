package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Muted;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.MutedMapper;
import com.dandelion.system.service.MutedService;
import com.dandelion.system.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Muted> mutedPage = new Page<>(currentPage, pageSize);
        IPage<Muted> page = mutedService.page(mutedPage);
        List<Muted> mutedList = page.getRecords();
        for (Muted muted : mutedList) {
            muted.setUser(userService.getOne(new LambdaQueryWrapper<User>().eq(User::getId,muted.getUserId())));
        }
        page.setRecords(mutedList);
        return ResponseResult.success(page, Massage.SELECT.value());
    }

//    @ApiOperation(value = "禁言用户查询",notes = "根据 用户名 查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byUserName/{userName}")
    public ResponseResult queryByUserName(@PathVariable String userName) {
        return ResponseResult.success(mutedMapper.getAllByUserName(userName), Massage.SELECT.value());
    }

//    @ApiOperation(value = "禁言用户查询",notes = "根据 userId 查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byId/{userId}")
    public ResponseResult queryById(@PathVariable String userId) {
        return ResponseResult.success(mutedService.list(new LambdaQueryWrapper<Muted>().eq(Muted::getUserId,userId)), Massage.SELECT.value());
    }

//    @ApiOperation(value = "添加用户禁言",notes = "根据 userId 添加")
    @Log(title = "封禁管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:add')")
    @PostMapping("/add/byUserId/{userId}/{day}")
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

//    @ApiOperation(value = "解除用户禁言",notes = "根据 userId 修改")
    @Log(title = "封禁管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:update')")
    @PostMapping("/update/byUserId/{userId}")
    public ResponseResult updateEffectiveByUserId(@PathVariable String userId){
        userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,0));
        mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getUserId,userId).set(Muted::getEffective,1).ne(Muted::getEffective,1));
        return ResponseResult.success("解除成功");
    }
}
