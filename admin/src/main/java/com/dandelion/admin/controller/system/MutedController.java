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

    @PreAuthorize("@dandelion.hasAuthority('system:muted:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Muted> mutedPage = new Page<>(currentPage, pageSize);
        IPage<Muted> page = mutedService.page(
                mutedPage,
                new LambdaQueryWrapper<Muted>().orderByDesc(Muted::getCreateTime));
        List<Muted> mutedList = page.getRecords();
        for (Muted muted : mutedList) {
            muted.setUser(userMapper.getUserVoById(muted.getUserId()));
        }
        return ResponseResult.success(page);
    }

    @PreAuthorize("@dandelion.hasAuthority('system:muted:query')")
    @GetMapping("/query/byUserName/{userName}")
    public ResponseResult queryByUserName(@RequestParam(defaultValue = "1") Integer currentPage,
                                          @RequestParam(defaultValue = "5") Integer pageSize,
                                          @PathVariable String userName) {
        Page<Muted> mutedPage = new Page<>(currentPage, pageSize);
        IPage<Muted> page = mutedMapper.getAllByUserName(
                mutedPage,
                new LambdaQueryWrapper<Muted>().orderByDesc(Muted::getCreateTime),
                userName);
        List<Muted> mutedList = page.getRecords();
        Assert.notEmpty(mutedList,"未找到该用户的封禁信息");
        for (Muted muted : mutedList) {
            muted.setUser(userMapper.getUserVoById(muted.getUserId()));
        }
        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @Log(title = "封禁管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:add')")
    @PostMapping("/add/byUserId/{userId}/{day}")
    public ResponseResult addByUserId(@PathVariable String userId,@PathVariable Integer day){
        mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getUserId,userId).set(Muted::getEffective,1));
        if (day == 0){
            userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,2));
            return ResponseResult.success("永久封禁成功!");
        }
        Set<String> keys = redisCache.scan("mutedUser-"+userId+ "-*");
        int newDay=day;
        String key="mutedUser-"+userId+"-"+newDay;
        if (keys.size()!=0){
            long keyExpire = redisCache.getKeyExpire(keys.iterator().next(), TimeUnit.MINUTES);
            redisCache.deleteObject(keys.iterator().next());
            newDay = Math.toIntExact(day + keyExpire)+1;
            key="mutedUser-"+userId+"-"+newDay;
        }
        Map<String, Object> map = redisCache.getCacheMap(key);
        Muted muted = new Muted();
        muted.setUserId(Long.valueOf(userId));
        muted.setCreateBy(SecurityUtils.getUsername());
        muted.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE,newDay);
        muted.setMutedTime(calendar.getTime());
        mutedService.update(new LambdaUpdateWrapper<Muted>().eq(Muted::getUserId,userId).set(Muted::getEffective,1));
        mutedService.save(muted);
        userService.update(new LambdaUpdateWrapper<User>().eq(User::getId,userId).set(User::getMuted,1));
        map.put("flag",true);
        redisCache.setCacheMap(key,map,newDay, TimeUnit.MINUTES);
        return ResponseResult.success("禁言成功!");
    }

    @Log(title = "封禁管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:muted:update')")
    @PostMapping("/update/{userId}/{mutedId}")
    public ResponseResult updateEffectiveByUserId(@PathVariable String userId, @PathVariable String mutedId){
        userService.update(new LambdaUpdateWrapper<User>()
                .eq(User::getId,userId)
                .set(User::getMuted,0));
        mutedService.update(new LambdaUpdateWrapper<Muted>()
                .eq(Muted::getId,mutedId)
                .eq(Muted::getUserId,userId)
                .set(Muted::getEffective,1));
        Set<String> keys = redisCache.scan("mutedUser-"+userId+ "-*");
        redisCache.deleteObject(keys);
        return ResponseResult.success("解封成功");
    }
}
