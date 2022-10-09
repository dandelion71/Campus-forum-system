package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import java.util.Date;

@RestController
@RequestMapping("/system/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @ApiOperation(value = "查询菜单", notes = "查询菜单列表")
    @GetMapping("/list")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:list')")
    public ResponseResult list() {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId,0)), Massage.SELECT.value());
    }

    @ApiOperation(value = "查询菜单", notes = "根据 id 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/{id}")
    public ResponseResult getOneMenu(@PathVariable Long id) {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getId, id)),Massage.SELECT.value());
    }

    @ApiOperation(value = "查询菜单名是否存在", notes = "根据 menuName 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/menuExists/{menuName}")
    public ResponseResult menuExists(@PathVariable String menuName) {
        Menu menu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getMenuName, menuName));
        Assert.isNull(menu,"菜单名已存在");
        return ResponseResult.success("菜单名不存在");
    }

    @ApiOperation(value = "查询子菜单", notes = "根据 父id 查询菜单")
    @PreAuthorize("@dandelion.hasAuthority('system:menu:query')")
    @GetMapping(value = "/list/{parentId}")
    public ResponseResult getChildMenu(@PathVariable Long parentId) {
        return ResponseResult.success(menuService.list(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentId)),Massage.SELECT.value());
    }

    @ApiOperation(value = "添加菜单")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:add')")
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Menu menu){
        menu.setCreateBy(SecurityUtils.getUsername());
        menu.setCreateTime(new Date());
        menuExists(menu.getMenuName());
        return ResponseResult.success(menuService.save(menu),Massage.SAVE.value());
    }

    @ApiOperation(value = "修改菜单")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:edit')")
    @PostMapping("/edit")
    public ResponseResult edit(@RequestBody Menu menu){
        menu.setUpdateBy(SecurityUtils.getUsername());
        menu.setUpdateTime(new Date());
        return ResponseResult.success(menuService.updateById(menu),Massage.UPDATE.value());
    }

    @ApiOperation(value = "修改菜单是否可用",notes = "根据 id 修改菜单状态")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:edit')")
    @PostMapping("/status/{status}/{id}")
    public ResponseResult editStatus(@PathVariable String id, @PathVariable String status){
        return ResponseResult.success(
                menuService.update(new LambdaUpdateWrapper<Menu>()
                        .eq(Menu::getId,id)
                        .set(Menu::getStatus,status)
                        .set(Menu::getUpdateBy,SecurityUtils.getUsername())
                        .set(Menu::getUpdateTime,new Date())),Massage.UPDATE.value());
    }

    @ApiOperation(value = "修改菜单是否隐藏",notes = "根据 id 修改菜单显隐")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:edit')")
    @PostMapping("/visible/{Visible}/{id}")
    public ResponseResult editVisible(@PathVariable String id, @PathVariable String Visible){
        return ResponseResult.success(
                menuService.update(new LambdaUpdateWrapper<Menu>()
                        .eq(Menu::getId,id)
                        .set(Menu::getVisible,Visible)
                        .set(Menu::getUpdateBy,SecurityUtils.getUsername())
                        .set(Menu::getUpdateTime,new Date())),Massage.UPDATE.value());
    }

    @ApiOperation(value = "删除菜单",notes = "根据 id 删除菜单")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@dandelion.hasAuthority('system:menu:remove')")
    @PostMapping("/remove/{id}")
    public ResponseResult remove(@PathVariable String id){
        menuService.removeById(id);
        return ResponseResult.success(null,Massage.DELETE.value());
    }
}
