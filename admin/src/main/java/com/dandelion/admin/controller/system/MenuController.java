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
import com.dandelion.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;

@RestController
@RequestMapping("/system/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:list')")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Menu> menuPage = new Page<>(currentPage, pageSize);
        IPage<Menu> page = menuService.page(menuPage, new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, 0).orderByAsc(Menu::getOrderNum));
        return ResponseResult.success(page);
    }

    @GetMapping("/queryMaster")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:list')")
    public ResponseResult queryMaster() {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, 0).orderByAsc(Menu::getOrderNum)));
    }

    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/query/byId/{id}")
    public ResponseResult byId(@PathVariable Long id) {
        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getId, id));
        Assert.notNull(menu,"未找到该菜单");
        return ResponseResult.success(menu,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/query/byMenuName/{menuName}")
    public ResponseResult byMenuName(@PathVariable String menuName) {
        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getMenuName, menuName));
        Assert.notNull(menu,"未找到该菜单");
        return ResponseResult.success(menu,Massage.SELECT.value());
    }

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
