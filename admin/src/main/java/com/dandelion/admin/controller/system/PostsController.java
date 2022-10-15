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
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.PostsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/posts")
public class PostsController {

    @Autowired
    private PostsService postsService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private SectionMapper sectionMapper;

    @ApiOperation(value = "帖子管理")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:list')")
    @GetMapping("/list/{sectionId}")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @PathVariable String sectionId) {
        Page<Posts> postsPage = new Page<>(currentPage, pageSize);
        IPage<Posts> page = postsService.page(postsPage, new LambdaQueryWrapper<Posts>()
                .eq(Posts::getSectionId,sectionId)
                .or().eq(Posts::getSectionId,0)
                .ne(Posts::getDelFlag, 2)
                .orderByDesc(Posts::getTop)
                .orderByDesc(Posts::getCreateTime));
        List<Posts> records = page.getRecords();
        for (Posts post : records) {
            post.setUser(userMapper.getUserVoById(post.getUserId()));
            post.setTag(tagMapper.getTagVoById(post.getTagId()));
            post.setSection(sectionMapper.getSectionVoById(post.getSectionId()));
        }
        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @ApiOperation(value = "帖子添加")
    @Log(title = "帖子管理",businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:add')")
    public ResponseResult add(@RequestBody Posts posts){
        posts.setUserId(SecurityUtils.getUserId());
        posts.setCreateTime(new Date());
        postsService.save(posts);
        return ResponseResult.success(Massage.SAVE.value());
    }

    @ApiOperation(value = "帖子修改")
    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    public ResponseResult edit(@RequestBody Posts posts){
        posts.setUpdateBy(SecurityUtils.getUsername());
        posts.setUpdateTime(new Date());
        postsService.updateById(posts);
        return ResponseResult.success(Massage.UPDATE.value());
    }
    @ApiOperation(value = "帖子修改",notes = "根据 ID 设置是否置顶")
    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editTop/{id}/{top}")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    public ResponseResult editTop(@PathVariable String id, @PathVariable String top){
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,id).set(Posts::getTop,top));
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "帖子修改",notes = "根据 ID 设置是否加精")
    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editElite/{id}/{elite}")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    public ResponseResult editElite(@PathVariable String id, @PathVariable String elite){
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,id).set(Posts::getElite,elite));
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "帖子删除")
    @Log(title = "帖子管理",businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:remove')")
    public ResponseResult remove(@PathVariable String id){
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,id).set(Posts::getDelFlag,2));
        return ResponseResult.success(Massage.DELETE.value());
    }


}
