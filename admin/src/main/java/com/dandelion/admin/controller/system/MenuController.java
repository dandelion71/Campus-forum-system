package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.service.MenuService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/system/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @ApiOperation(value = "查询菜单", notes = "查询菜单列表")
    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:list')")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Menu> menuPage = new Page<>(currentPage, pageSize);
        IPage<Menu> page = menuService.page(menuPage, new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, 0).orderByAsc(Menu::getOrderNum));
        return ResponseResult.success(page);
    }

    @ApiOperation(value = "查询菜单", notes = "查询父菜单列表")
    @GetMapping("/queryMaster")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:list')")
    public ResponseResult queryMaster() {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, 0).orderByAsc(Menu::getOrderNum)));
    }

//    @ApiOperation(value = "查询菜单", notes = "根据 id 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/query/byId/{id}")
    public ResponseResult byId(@PathVariable Long id) {
        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getId, id));
        Assert.notNull(menu,"未找到该菜单");
        return ResponseResult.success(menu,Massage.SELECT.value());
    }

//    @ApiOperation(value = "查询菜单名是否存在", notes = "根据 menuName 查询菜单")
//    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
//    @GetMapping(value = "/menuExists/{menuName}")
//    public ResponseResult menuExists(@PathVariable String menuName) {
//        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getMenuName, menuName));
//        Assert.isNull(menu,"菜单名已存在");
//        return ResponseResult.success();
//    }

    //    @ApiOperation(value = "查询菜单", notes = "根据 menuName 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/query/byMenuName/{menuName}")
    public ResponseResult byMenuName(@PathVariable String menuName) {
        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getMenuName, menuName));
        Assert.notNull(menu,"未找到该菜单");
        return ResponseResult.success(menu,Massage.SELECT.value());
    }

//    @ApiOperation(value = "查询子菜单", notes = "根据 父id 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/list/{parentId}")
    public ResponseResult getChildMenu(@PathVariable Long parentId) {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentId).orderByAsc(Menu::getOrderNum)));
    }

//    @ApiOperation(value = "修改菜单")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:edit')")
    @PostMapping("/edit")
    public ResponseResult edit(@RequestBody Menu menu){
        if(Objects.nonNull(menu.getMenuName())){
            Assert.isNull(menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getMenuName, menu.getMenuName())),"菜单名已存在");
        }
        menu.setUpdateBy(SecurityUtils.getUsername());
        menu.setUpdateTime(new Date());
        return ResponseResult.success(menuService.updateById(menu),Massage.UPDATE.value());
    }
}
