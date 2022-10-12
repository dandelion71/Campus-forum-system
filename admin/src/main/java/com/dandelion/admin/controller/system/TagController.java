package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Tag;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.service.TagService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/system/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagMapper tagMapper;

    @ApiOperation(value = "分类管理")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Tag> tagPage = new Page<>(currentPage, pageSize);
        IPage<Tag> page = tagService.page(tagPage);
        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @ApiOperation(value = "分类查询",notes = "根据 ID 查询分类")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/query/ById/{id}")
    public ResponseResult queryById(@PathVariable String id){
        return ResponseResult.success(tagService.getById(id),Massage.SELECT.value());
    }

    @ApiOperation(value = "分类查询",notes = "根据 tagName 查询分类")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/query/ByTagName/{tagName}")
    public ResponseResult queryByTagName(@PathVariable String tagName){
        return ResponseResult.success(tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName,tagName)),Massage.SELECT.value());
    }

    @ApiOperation(value = "分类添加")
    @Log(title = "分类管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:tag:add')")
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Tag tag){
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(new Date());
        tagService.save(tag);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @ApiOperation(value = "分类修改",notes = "根据 id 修改分类")
    @Log(title = "分类管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:edit')")
    public ResponseResult edit(@RequestBody Tag tag){
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(new Date());
        tagService.updateById(tag);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "分类删除",notes = "根据 tagId 删除分类")
    @Log(title = "分类管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{oldTagId}/{newTagId}")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:remove')")
    public ResponseResult remove(@PathVariable String oldTagId, @PathVariable String newTagId){
        Tag tag = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getId, oldTagId).ne(Tag::getIsDel, "1"));
        Assert.notNull(tag,"该分类不可删除");
        tagMapper.deleteSectionTagByTagId(oldTagId);
        tagMapper.updatePostsTagId(oldTagId,newTagId);
        tagService.removeById(oldTagId);
        return ResponseResult.success(Massage.DELETE.value());
    }
}
