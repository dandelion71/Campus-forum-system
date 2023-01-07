package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Muted;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.MutedMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.MutedService;
import com.dandelion.system.service.UserService;
import com.dandelion.system.vo.PostsSimpleVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/system/muted")
public class MutedController {

    @Autowired
    private MutedService mutedService;

    @Autowired
    private MutedMapper mutedMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisCache redisCache;


    @ApiOperation(value = "禁言用户查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Muted> mutedPage = new Page<>(currentPage, pageSize);
        IPage<Muted> page = mutedService.page(mutedPage,new LambdaQueryWrapper<Muted>().orderByDesc(Muted::getCreateTime));
        List<Muted> mutedList = page.getRecords();
        for (Muted muted : mutedList) {
            muted.setEffective(checkMuted(muted.getUserId().toString()));
            muted.setUser(userMapper.getUserVoById(muted.getUserId()));
        }
        return ResponseResult.success(page);
    }

//    @ApiOperation(value = "禁言用户查询",notes = "根据 用户名 查询")
    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byUserName/{userName}")
    public ResponseResult queryByUserName(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize,@PathVariable String userName) {
        Page<Muted> mutedPage = new Page<>(currentPage, pageSize);
        IPage<Muted> page = mutedMapper.getAllByUserName(mutedPage,new LambdaQueryWrapper<Muted>().orderByDesc(Muted::getCreateTime),userName);
        List<Muted> mutedList = page.getRecords();
        Assert.notEmpty(mutedList,"未找到该用户的封禁信息");
        for (Muted muted : mutedList) {
            muted.setEffective(checkMuted(muted.getUserId().toString()));
            muted.setUser(userMapper.getUserVoById(muted.getUserId()));
        }
        return ResponseResult.success(page, Massage.SELECT.value());
    }

//    @ApiOperation(value = "禁言用户查询",notes = "根据 userId 查询")
//    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
//    @GetMapping("/query/byId/{userId}")
//    public ResponseResult queryById(@PathVariable String userId) {
//        List<Muted> mutedList = mutedService.list(new LambdaQueryWrapper<Muted>().eq(Muted::getUserId, userId));
//        Assert.notNull(mutedList,"未找到该用户的封禁信息");
//        return ResponseResult.success(mutedList, Massage.SELECT.value());
//    }

//    @ApiOperation(value = "添加用户禁言",notes = "根据 userId 添加")
    @Log(title = "封禁管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:add')")
    @PostMapping("/add/byUserId/{userId}/{day}")
    public ResponseResult addByUserId(@PathVariable String userId,@PathVariable Integer day){
        mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getUserId,userId).set(Muted::getEffective,1));
        if (day == 0){
            userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,2));
            return ResponseResult.success("永久封禁成功!");
        }
        String key = "mutedUser-"+userId;
        Map<String, Object> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            Muted muted = new Muted();
            muted.setUserId(Long.valueOf(userId));
            muted.setCreateBy(SecurityUtils.getUsername());
            muted.setCreateTime(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE,day);
            muted.setMutedTime(calendar.getTime());
            mutedService.save(muted);
            userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,1));
            map.put("flag",true);
            redisCache.setCacheMap(key,map,day, TimeUnit.MINUTES);
        }
        return ResponseResult.success("禁言成功!");
    }

//    @ApiOperation(value = "解除用户禁言",notes = "根据 userId 修改")
    @Log(title = "封禁管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:update')")
    @PostMapping("/update/{userId}/{mutedId}")
    public ResponseResult updateEffectiveByUserId(@PathVariable String userId, @PathVariable String mutedId){
        userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,0));
        mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getId,mutedId).eq(Muted::getUserId,userId).set(Muted::getEffective,1));
        redisCache.deleteObject("mutedUser-"+userId);
        return ResponseResult.success("解封成功");
    }

    private String checkMuted(String userId){
        if (!redisCache.existKey("mutedUser-"+userId)) {
            String muted = userService.getObj(new LambdaQueryWrapper<User>().select(User::getMuted).eq(User::getId,userId),Object::toString);
            if ("1".equals(muted)) {
                userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,0));
                mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getUserId, userId).set(Muted::getEffective, 1));
            }
            return "1";
        }
        return "0";
    }
}
