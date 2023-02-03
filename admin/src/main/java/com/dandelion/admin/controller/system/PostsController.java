package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.PostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private RedisCache redisCache;

    @PreAuthorize("@dandelion.hasAuthority('system:posts:list')")
    @GetMapping("/list")
    public ResponseResult list(@RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "5") Integer pageSize,
                               @RequestParam(defaultValue = "0") String key,
                               @RequestParam(defaultValue = "0") String value) {
        LambdaQueryWrapper<Posts> queryWrapper = new LambdaQueryWrapper<>();
        switch (key){
            case "0":break;
            case "1":queryWrapper.eq(Posts::getUserId,value);break;
            case "2":queryWrapper.eq(Posts::getSectionId,value);break;
            case "3":queryWrapper.like(Posts::getTitle,value);break;
            case "4":queryWrapper.eq(Posts::getId,value);break;
        }
        Page<Posts> postsPage = new Page<>(currentPage, pageSize);
        IPage<Posts> page = postsService.page(
                postsPage,
                queryWrapper.orderByDesc(Posts::getCreateTime));
        List<Posts> records = page.getRecords();
        for (Posts post : records) {
            post.setUser(userMapper.getUserVoById(post.getUserId()));
            post.setTag(tagMapper.getTagVoById(post.getTagId()));
            post.setSection(sectionMapper.getSectionVoById(post.getSectionId()));
        }
        return ResponseResult.success(page);
    }

    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    @PostMapping("/editStatus/{id}/{top}")
    public ResponseResult editStatus(@PathVariable String id, @PathVariable String top){
        postsService.update(new LambdaUpdateWrapper<Posts>()
                .eq(Posts::getId,id)
                .set(Posts::getStatus,top));
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    @PostMapping("/editTop/{id}/{top}")
    public ResponseResult editTop(@PathVariable String id, @PathVariable String top){
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,id).set(Posts::getTop,top));
        Posts posts = postsService.getById(id);
        redisCache.deleteObject(redisCache.scan("commentTime-"+posts.getSectionId()+"-0-*"));
        redisCache.deleteObject(redisCache.scan("queryPost-"+ posts.getId() +"-*"));
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editElite/{id}/{elite}")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:edit')")
    public ResponseResult editElite(@PathVariable String id, @PathVariable String elite){
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,id).set(Posts::getElite,elite));
        redisCache.deleteObject("queryNewElitePost");
        Posts posts = postsService.getById(id);
        redisCache.deleteObject(redisCache.scan("queryPostUser-*-"+posts.getUserId()));
        redisCache.deleteObject(redisCache.scan("queryPost-"+ posts.getId() +"-*"));
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @Log(title = "帖子管理",businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:remove')")
    public ResponseResult remove(@PathVariable String id){
        postsService.update(
                new LambdaUpdateWrapper<Posts>()
                        .eq(Posts::getId,id)
                        .set(Posts::getDelFlag,2));
        return ResponseResult.success(Massage.DELETE.value());
    }
}
