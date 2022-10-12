package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Section;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.SectionService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/section")
public class SectionController {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private PostsService postsService;

    @Autowired
    private SectionMapper sectionMapper;

    @ApiOperation(value = "版块管理")
    @PreAuthorize("@dandelion.hasAuthority('system:section:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Section> sectionPage = new Page<>(currentPage, pageSize);
        IPage<Section> page = sectionService.page(sectionPage, new LambdaQueryWrapper<Section>().eq(Section::getParentId, 0));
        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @ApiOperation(value = "分区查询",notes = "查询分区以及分区下的版块")
    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query")
    public ResponseResult query(){
        List<Section> sectionList = sectionService.list(
                new LambdaQueryWrapper<Section>()
                        .eq(Section::getParentId, 0)
                        .ne(Section::getStatus,1));
        for (Section section : sectionList) {
            section.setSection(sectionService.list(
                    new LambdaQueryWrapper<Section>()
                            .eq(Section::getParentId,section.getId())
                            .ne(Section::getStatus,1)));
        }
        return ResponseResult.success(sectionList,Massage.SELECT.value());
    }


    @ApiOperation(value = "版块查询",notes = "根据 parentId 查询分区下的版块")
    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/{parentId}")
    public ResponseResult queryByParentId(@PathVariable String parentId){
        return ResponseResult.success(sectionService.list(new LambdaQueryWrapper<Section>().eq(Section::getParentId,parentId)),Massage.SELECT.value());
    }

    @ApiOperation(value = "版块查询",notes = "根据 sectionId 查询版块拥有的分类")
    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/haveTag/{sectionId}")
    public ResponseResult queryByHaveTag(@PathVariable String sectionId){
        return ResponseResult.success(sectionMapper.selectHaveTagBySectionId(sectionId),Massage.SELECT.value());
    }

    @ApiOperation(value = "版块查询",notes = "根据 sectionId 查询版块未拥有的分类")
    @PreAuthorize("@dandelion.hasAuthority('system:section:query')")
    @GetMapping("/query/noneTag/{sectionId}")
    public ResponseResult queryByNoneTag(@PathVariable String sectionId){
        return ResponseResult.success(sectionMapper.selectNoneTagBySectionId(sectionId),Massage.SELECT.value());
    }


    @ApiOperation(value = "分区（版块）添加")
    @Log(title = "版块管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:section:add')")
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Section section){
        section.setCreateBy(SecurityUtils.getUsername());
        section.setCreateTime(new Date());
        sectionService.save(section);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @ApiOperation(value = "分区（版块）添加",notes = "根据 sectionId tagId 添加分类")
    @Log(title = "版块管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:section:add')")
    @PostMapping("/addTag/{sectionId}")
    public ResponseResult addTag(@PathVariable String sectionId,@RequestBody List<String> tagIds){
        for (String tagId : tagIds) {
            sectionMapper.insertSectionTag(sectionId,tagId);
        }
        return ResponseResult.success(Massage.SAVE.value());
    }

    @ApiOperation(value = "分区（版块）修改",notes = "根据 id 修改分区（版块）")
    @Log(title = "版块管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:section:edit')")
    public ResponseResult edit(@RequestBody Section section){
        section.setUpdateBy(SecurityUtils.getUsername());
        section.setUpdateTime(new Date());
        sectionService.updateById(section);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "分区（版块）删除",notes = "根据 id 删除分区（版块）")
    @Log(title = "版块管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:section:remove')")
    public ResponseResult removeById(@PathVariable String id){
        Section section = sectionService.getOne(new LambdaQueryWrapper<Section>().eq(Section::getId,id).ne(Section::getIsDel,'1'));
        Assert.notNull(section,"该分区（版块）不可删除");
        if(section.getParentId() == 0L){
            //删除分区
            List<Section> sectionList = sectionService.list(new LambdaQueryWrapper<Section>().eq(Section::getParentId, id));
            for (Section section1 : sectionList) {
                postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getSectionId,section1.getId()).set(Posts::getDelFlag,2));
            }
            sectionService.remove(new LambdaQueryWrapper<Section>().eq(Section::getParentId,id));
        }else {
            //删除版块
            postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getSectionId,section.getId()).set(Posts::getDelFlag,2));
        }
        sectionService.removeById(id);
        return ResponseResult.success(Massage.DELETE.value());
    }

}
