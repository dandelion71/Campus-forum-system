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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/system/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagMapper tagMapper;

    @PreAuthorize("@dandelion.hasAuthority('system:tag:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,@RequestParam(defaultValue = "5") Integer pageSize) {
        Page<Tag> tagPage = new Page<>(currentPage, pageSize);
        IPage<Tag> page = tagService.page(tagPage);
        return ResponseResult.success(page);
    }

    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/queryAll")
    public ResponseResult queryAll(){
        return ResponseResult.success(tagService.list());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/query/ById/{id}")
    public ResponseResult queryById(@PathVariable String id){
        Tag tag = tagService.getById(id);
        Assert.notNull(tag,"未找到该分类");
        return ResponseResult.success(tag,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/query/BySectionId/{sectionId}")
    public ResponseResult queryBySectionId(@PathVariable String sectionId){
        List<Long> tagIds = tagMapper.getSectionTag(sectionId);
        return ResponseResult.success(tagIds,Massage.SELECT.value());
    }

    @PreAuthorize("@dandelion.hasAuthority('system:tag:query')")
    @GetMapping("/query/ByTagName/{tagName}")
    public ResponseResult queryByTagName(@PathVariable String tagName){
        Tag tag = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tagName));
        Assert.notNull(tag,"未找到该分类");
        return ResponseResult.success(tag,Massage.SELECT.value());
    }

    @Log(title = "分类管理",businessType = BusinessType.INSERT)
    @PreAuthorize("@dandelion.hasAuthority('system:tag:add')")
    @PostMapping("/add")
    public ResponseResult add(@RequestBody Tag tag){
        if(Objects.nonNull(tag.getTagName())){
            Assert.isNull(tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tag.getTagName())), "分类名已存在");
        }
        tag.setCreateBy(SecurityUtils.getUsername());
        tag.setCreateTime(new Date());
        tagService.save(tag);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @Log(title = "分类管理", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:edit')")
    public ResponseResult edit(@RequestBody Tag tag){
        if(Objects.nonNull(tag.getTagName())) {
            Assert.isNull(tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tag.getTagName())), "分类名已存在");
        }
        tag.setUpdateBy(SecurityUtils.getUsername());
        tag.setUpdateTime(new Date());
        tagService.updateById(tag);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "分类管理", businessType = BusinessType.DELETE)
    @PostMapping("/remove/{tagId}")
    @PreAuthorize("@dandelion.hasAuthority('system:tag:remove')")
    public ResponseResult remove(@PathVariable String tagId){
        Tag tag = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getId, tagId).ne(Tag::getIsDel, "1"));
        Assert.notNull(tag,"该分类不可删除");
        tagMapper.deleteSectionTagByTagId(tagId);
        tagMapper.updatePostsTagId(tagId,"2");
        tagService.removeById(tagId);
        return ResponseResult.success(Massage.DELETE.value());
    }
}
