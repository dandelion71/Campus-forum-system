package com.dandelion.admin.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.system.dao.*;
import com.dandelion.system.mapper.PostsMapper;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.vo.CollectionVo;
import com.dandelion.system.vo.LikesVo;
import com.dandelion.system.vo.PostsUpdateVo;
import com.dandelion.system.vo.PostsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/reception/posts")
public class ReceptionPostsController {

    @Autowired
    private PostsMapper postsMapper;

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

    @GetMapping("/queryNewPost")
    public ResponseResult queryNewPost(){
        return ResponseResult.success(postsMapper.selectNewPost());
    }

    @GetMapping("/queryNewElitePost")
    public ResponseResult queryNewElitePost(){
        return ResponseResult.success(postsMapper.selectNewElitePost());
    }

    @GetMapping("/queryNewPostComment")
    public ResponseResult queryNewPostComment(){
        return ResponseResult.success(postsMapper.selectNewPostComment());
    }

    @GetMapping("/queryHotPost/{sectionId}")
    public ResponseResult queryHotPost(@PathVariable String sectionId){
        return ResponseResult.success(postsMapper.selectHotPost(sectionId));
    }

    @GetMapping("/queryUpdatePost/{postId}")
    public ResponseResult queryUpdatePost(@PathVariable String postId){
        return ResponseResult.success(postsMapper.selectPostUpdateVo(postId));
    }

    @GetMapping("/searchPost")
    public ResponseResult search(@RequestParam(defaultValue = "1") Integer currentPage,
                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                 @RequestParam String value){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByKeyword(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),value);
        setPostsInfo(page);
        return ResponseResult.success(page,null);
    }


    @GetMapping("/queryPost/{postId}")
    public ResponseResult queryPost(@PathVariable String postId){
        Posts post = postsService.getOne(new LambdaQueryWrapper<Posts>().eq(Posts::getId,postId).ne(Posts::getDelFlag,2));
        Assert.notNull(post,"该帖子已被删除");
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,post.getId()).set(Posts::getSeeNum,post.getSeeNum()+1));
        post.setUser(userMapper.getUserVoById(post.getUserId()));
        post.setTag(tagMapper.getTagVoById(post.getTagId()));
        post.setSection(sectionMapper.getSectionVoById(post.getSectionId()));
        Object principal = SecurityUtils.getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)){
            post.setIsUserLike(true);
            post.setIsUserCollection(true);
            post.setIsEditPost(false);
        }else {
            LikesVo likes = postsMapper.selectLikes(postId, SecurityUtils.getUserId().toString());
            if (Objects.isNull(likes)){
                post.setIsUserLike(true);
            }else {
                post.setIsUserLike(likes.getIsLike().equals("0"));
            }
            CollectionVo collection = postsMapper.selectCollection(postId,SecurityUtils.getUserId().toString());
            if (Objects.isNull(collection)){
                post.setIsUserCollection(true);
            }else {
                post.setIsUserCollection(collection.getIsCollection().equals("0"));
            }
            if(SecurityUtils.isAdmin(SecurityUtils.getUserId())){
                post.setIsEditPost(true);
            }else if(StringUtils.isNotEmpty(sectionMapper.getSectionUser(SecurityUtils.getUserId(),post.getSectionId().toString()))){
                post.setIsEditPost(true);
            }else post.setIsEditPost(SecurityUtils.getUserId().equals(post.getUserId()));
        }
        return ResponseResult.success(post);
    }

    @GetMapping("/commentTime/{sectionId}")
    public ResponseResult queryDefaultPost(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectDefaultPost(sectionId, tagId, postPage);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/postTime/{sectionId}")
    public ResponseResult queryPostTime(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("create_time"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/mostLike/{sectionId}")
    public ResponseResult queryMostLike(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("likes_num"),sectionId, tagId);
        List<PostsVo> records = page.getRecords();
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/mostHot/{sectionId}")
    public ResponseResult queryMostHot(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("see_num*1+p.likes_num*2+collection_num*3"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/mostSee/{sectionId}")
    public ResponseResult queryMostSee(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("see_num"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }


    @GetMapping("/queryAllByUser/{userId}")
    public ResponseResult queryAllByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @PathVariable String userId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectAllPostByUser(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),userId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/queryCollectionByUser/{userId}")
    public ResponseResult queryCollectionByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @PathVariable String userId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByUserCollection(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),userId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/queryEliteByUser/{userId}")
    public ResponseResult queryEliteByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @PathVariable String userId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectElitePostByUser(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),userId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }




    @PreAuthorize("@dandelion.hasAuthority('user:posts:add')")
    @PostMapping("/addPost")
    public ResponseResult addPost(@RequestBody Posts post){
        post.setUserId(SecurityUtils.getUserId());
        post.setCreateTime(new Date());
        postsService.save(post);
        redisCache.deleteObject("topNums");
        return ResponseResult.success(post.getId(),"");
    }

    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('user:posts:edit')")
    @PostMapping("/editPost")
    public ResponseResult editPost(@RequestBody Posts post){
        post.setUpdateBy(SecurityUtils.getUsername());
        post.setUpdateTime(new Date());
        postsService.updateById(post);
        return ResponseResult.success(post.getId(),"");
    }

    @PreAuthorize("@dandelion.hasAuthority('user:posts:likes')")
    @PostMapping("/addLikes/{postId}")
    public ResponseResult addLikes(@PathVariable String postId){
        String userId = SecurityUtils.getUserId().toString();
        LikesVo likes = postsMapper.selectLikes(postId,userId);
        if (Objects.isNull(likes)){
            postsMapper.insertLikes(postId,userId);
        }else {
            if (likes.getIsLike().equals("0")){
                postsMapper.updateLikes(postId,userId,"1");
            }else {
                postsMapper.updateLikes(postId,userId,"0");
            }
        }
        Long likesNum = postsMapper.selectLikesNum(postId);
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,postId).set(Posts::getLikesNum,likesNum));
        return ResponseResult.success(likesNum);
    }

    @PreAuthorize("@dandelion.hasAuthority('user:posts:collection')")
    @PostMapping("/addCollection/{postId}")
    public ResponseResult addCollection(@PathVariable String postId){
        String userId = SecurityUtils.getUserId().toString();
        CollectionVo collection = postsMapper.selectCollection(postId,userId);
        if (Objects.isNull(collection)){
            postsMapper.insertCollection(postId,userId);
        }else {
            if (collection.getIsCollection().equals("0")){
                postsMapper.updateCollection(postId,userId,"1");
            }else {
                postsMapper.updateCollection(postId,userId,"0");
            }
        }
        Long collectionNum = postsMapper.selectCollectionNum(postId);
        postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,postId).set(Posts::getCollectionNum,collectionNum));
        return ResponseResult.success(collectionNum);
    }




    private void setPostsInfo(IPage<PostsVo> page){
        List<PostsVo> records = page.getRecords();
        for (PostsVo post : records) {
            post.setUser(userMapper.getUserVoById(post.getUserId()));
            post.setTag(tagMapper.getTagVoById(post.getTagId()));
            post.setSection(sectionMapper.getSectionVoById(post.getSectionId()));
        }
    }
}
