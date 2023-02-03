package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.*;
import com.dandelion.system.mapper.RoleMapper;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.SectionService;
import com.dandelion.system.vo.SectionMasterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/system/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private PostsService postsService;

    @Autowired
    private SectionMapper sectionMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@dandelion.hasAuthority('system:section:list')")
    @GetMapping("/list")
    public ResponseResult list() {
        return ResponseResult.success(sectionService.list(new LambdaQueryWrapper<Section>().eq(Section::getParentId, 0).orderByAsc(Section::getOrderNum)));
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query")
    public ResponseResult query(){
        List<SectionMasterVo> sectionMasterVos = sectionMapper.selectSection(
                new QueryWrapper<SectionMasterVo>().eq("parent_id","0"));
        for (SectionMasterVo sectionMasterVo : sectionMasterVos) {
            sectionMasterVo.setChildren(sectionMapper.selectSection(
                    new QueryWrapper<SectionMasterVo>().eq("parent_id", sectionMasterVo.getId())));
        }
        return ResponseResult.success(sectionMasterVos, Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/{parentId}")
    public ResponseResult queryByParentId(@RequestParam(defaultValue = "1") Integer currentPage,
                                          @RequestParam(defaultValue = "5") Integer pageSize,
                                          @PathVariable String parentId){
        Page<Section> sectionPage = new Page<>(currentPage, pageSize);
        IPage<Section> page = sectionService.page(
                sectionPage,
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getParentId,parentId)
                        .orderByAsc(Section::getOrderNum));
        return ResponseResult.success(page,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/byId/{sectionId}")
    public ResponseResult queryById(@PathVariable String sectionId){
        Section service = sectionService.getById(sectionId);
        Assert.notNull(service,"未找到该版块");
        return ResponseResult.success(service,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/queryModerator/byId/{sectionId}")
    public ResponseResult queryModerator(@PathVariable String sectionId){
        return ResponseResult.success(sectionMapper.getSectionModerator(sectionId));
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/byName/{sectionName}")
    public ResponseResult queryByName(@PathVariable String sectionName){
        Section service = sectionService.getOne(new LambdaQueryWrapper<Section>()
                .eq(Section::getSectionName,sectionName));
        Assert.notNull(service,"未找到该版块");
        return ResponseResult.success(service,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:section:add')")
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Section section){
        if(Objects.nonNull(section.getSectionName())){
            Assert.isNull(sectionService.getOne(
                    new LambdaQueryWrapper<Section>()
                            .eq(Section::getSectionName, section.getSectionName())),
                    "分区（版块）名已存在");
        }
        section.setCreateBy(SecurityUtils.getUsername());
        section.setCreateTime(new Date());
        sectionService.save(section);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @Log(title = "版块管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:section:add')")
    @PostMapping("/addModerator/{sectionId}")
    public ResponseResult addModerator(@PathVariable String sectionId,
                                       @RequestBody List<String> userIds){
        for (String userId : userIds) {
            sectionMapper.insertSectionUser(sectionId,userId);
            roleMapper.updateRoleByUserId(Long.valueOf(userId), 2L);
        }
        redisCache.deleteObject("queryModerator-"+sectionId);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @Log(title = "版块管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:section:add')")
    @PostMapping("/addTag/{sectionId}")
    public ResponseResult addTag(@PathVariable String sectionId,
                                 @RequestBody List<String> tagIds){
        for (String tagId : tagIds) {
            sectionMapper.insertSectionTag(sectionId,tagId);
        }
        redisCache.deleteObject("querySectionVo-"+sectionId);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @Log(title = "版块管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:section:edit')")
    public ResponseResult edit(@RequestBody Section section){
        if(Objects.nonNull(section.getSectionName())){
            Assert.isNull(sectionService.getOne(
                    new LambdaQueryWrapper<Section>()
                            .eq(Section::getSectionName, section.getSectionName())),
                    "版块名已存在");
        }
        section.setUpdateBy(SecurityUtils.getUsername());
        section.setUpdateTime(new Date());
        sectionService.updateById(section);
        redisCache.deleteObject("queryTopSection");
        redisCache.deleteObject("queryAllSection");
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "版块管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit/{sectionId}/{status}")
    @PreAuthorize("@dandelion.hasAuthority('system:section:edit')")
    public ResponseResult editStatus(@PathVariable String sectionId, @PathVariable String status){
        sectionService.update(new LambdaUpdateWrapper<Section>()
                .eq(Section::getId,sectionId)
                .set(Section::getStatus,status)
                .set(Section::getUpdateBy,SecurityUtils.getUsername())
                .set(Section::getUpdateTime,new Date()));
        redisCache.deleteObject("queryTopSection");
        redisCache.deleteObject("queryAllSection");
        return ResponseResult.success();
    }

    @Log(title = "版块管理",businessType = BusinessType.DELETE)
    @PreAuthorize("@dandelion.hasAuthority('system:section:remove')")
    @PostMapping("/delModerator/{sectionId}")
    public ResponseResult delModerator(@PathVariable String sectionId,
                                       @RequestBody List<String> userIds){
        for (String userId : userIds) {
            sectionMapper.delSectionUser(sectionId,userId);
            roleMapper.updateRoleByUserId(Long.valueOf(userId), 3L);
        }
        redisCache.deleteObject("queryModerator-"+sectionId);
        return ResponseResult.success(Massage.DELETE.value());
    }

    @Log(title = "版块管理",businessType = BusinessType.DELETE)
    @PreAuthorize("@dandelion.hasAuthority('system:section:remove')")
    @PostMapping("/delTag/{sectionId}")
    public ResponseResult delTag(@PathVariable String sectionId,
                                 @RequestBody List<String> tagIds){
        for (String tagId : tagIds) {
            sectionMapper.delSectionTag(sectionId,tagId);
        }
        redisCache.deleteObject("querySectionVo-"+sectionId);
        return ResponseResult.success(Massage.DELETE.value());
    }

    @Log(title = "版块管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:section:remove')")
    public ResponseResult removeById(@PathVariable String id){
        Section section = sectionService.getOne(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getId,id)
                        .ne(Section::getIsDel,'1'));
        Assert.notNull(section,"该版块不可删除");
        postsService.update(
                new LambdaUpdateWrapper<Posts>()
                        .eq(Posts::getSectionId,section.getId())
                        .set(Posts::getDelFlag,2));
        sectionService.removeById(id);
        return ResponseResult.success(Massage.DELETE.value());
    }

}
