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
import com.dandelion.system.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
        String key = "queryNewPost";
        Map<String, List<PostsSimpleVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",postsMapper.selectNewPost());
            redisCache.setCacheMap(key,map,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryNewElitePost")
    public ResponseResult queryNewElitePost(){
        String key = "queryNewElitePost";
        Map<String, List<PostsSimpleVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",postsMapper.selectNewElitePost());
            redisCache.setCacheMap(key,map,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryNewPostComment")
    public ResponseResult queryNewPostComment(){
        String key = "queryNewPostComment";
        Map<String, List<PostsSimpleVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",postsMapper.selectNewPostComment());
            redisCache.setCacheMap(key,map,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryHotPost/{sectionId}")
    public ResponseResult queryHotPost(@PathVariable String sectionId){
        String key = "queryHotPost-"+sectionId;
        Map<String, List<PostsSimpleVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",postsMapper.selectHotPost(sectionId));
            redisCache.setCacheMap(key,map,1, TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryUpdatePost/{postId}")
    public ResponseResult queryUpdatePost(@PathVariable String postId){
        return ResponseResult.success(postsMapper.selectPostUpdateVo(postId));
    }

    @GetMapping("/searchPost")
    public ResponseResult search(@RequestParam(defaultValue = "1") Integer currentPage,
                                 @RequestParam(defaultValue = "20") Integer pageSize,
                                 @RequestParam String value){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByKeyword(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),value);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }


    @GetMapping("/queryPost/{postId}")
    public ResponseResult queryPost(@PathVariable String postId){
        Object principal = SecurityUtils.getAuthentication().getPrincipal();
        String key;
        if ("anonymousUser".equals(principal)){
            key = "queryPost-"+ postId + "-anonymousUser";
        }else {
            key = "queryPost-"+ postId + "-" + SecurityUtils.getUserId();
        }
        Map<String, Posts> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)) {
            Posts post = postsService.getOne(new LambdaQueryWrapper<Posts>().eq(Posts::getId,postId).ne(Posts::getDelFlag,2));
            Assert.notNull(post,"该帖子已被删除");
            postsService.update(new LambdaUpdateWrapper<Posts>().eq(Posts::getId,post.getId()).set(Posts::getSeeNum,post.getSeeNum()+1));
            post.setUser(userMapper.getUserVoById(post.getUserId()));
            post.setTag(tagMapper.getTagVoById(post.getTagId()));
            post.setSection(sectionMapper.getSectionVoById(post.getSectionId()));
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
            map.put("bean",post);
            redisCache.setCacheMap(key,map,10,TimeUnit.MINUTES);
        }
        return ResponseResult.success(map.get("bean"));
    }

    @GetMapping("/commentTime/{sectionId}")
    public ResponseResult queryDefaultPost(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "20") Integer pageSize,
                                       @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        String key = "commentTime-"+sectionId+"-"+tagId+"-"+currentPage;
        Map<String, IPage<PostsVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
            IPage<PostsVo> page = postsMapper.selectDefaultPost(sectionId, tagId, postPage);
            setPostsInfo(page);
            map.put("page",page);
            redisCache.setCacheMap(key,map,1,TimeUnit.HOURS);
        }
        return ResponseResult.success(map.get("page"));
    }

    @GetMapping("/postTime/{sectionId}")
    public ResponseResult queryPostTime(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        String key = "postTime-"+sectionId+"-"+tagId+"-"+currentPage;
        Map<String, IPage<PostsVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
            IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                    new QueryWrapper<PostsVo>().orderByDesc("create_time"),sectionId, tagId);
            setPostsInfo(page);
            map.put("page",page);
            redisCache.setCacheMap(key,map,5,TimeUnit.MINUTES);
        }
        return ResponseResult.success(map.get("page"));
    }

    @GetMapping("/mostLike/{sectionId}")
    public ResponseResult queryMostLike(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("likes_num"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/mostHot/{sectionId}")
    public ResponseResult queryMostHot(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("see_num*1+p.likes_num*2+collection_num*3"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/mostSee/{sectionId}")
    public ResponseResult queryMostSee(@RequestParam(defaultValue = "1") Integer currentPage,
                                           @RequestParam(defaultValue = "20") Integer pageSize,
                                           @PathVariable String sectionId,@RequestParam(defaultValue = "0") String tagId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByTabs(postPage,
                new QueryWrapper<PostsVo>().orderByDesc("see_num"),sectionId, tagId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }


    @GetMapping("/queryAllByUser/{userId}")
    public ResponseResult queryAllByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                             @RequestParam(defaultValue = "20") Integer pageSize,
                                             @PathVariable String userId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectAllPostByUser(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),userId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/queryCollectionByUser/{userId}")
    public ResponseResult queryCollectionByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "20") Integer pageSize,
                                               @PathVariable String userId){
        Page<PostsVo> postPage = new Page<>(currentPage, pageSize);
        IPage<PostsVo> page = postsMapper.selectPostByUserCollection(postPage,new QueryWrapper<PostsVo>().orderByDesc("create_time"),userId);
        setPostsInfo(page);
        return ResponseResult.success(page);
    }

    @GetMapping("/queryEliteByUser/{userId}")
    public ResponseResult queryEliteByUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                             @RequestParam(defaultValue = "20") Integer pageSize,
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
        redisCache.deleteObject("queryNewPost");
        redisCache.deleteObject("querySectionById");
        redisCache.deleteObject(redisCache.scan("postTime-"+post.getSectionId()+"-0-*"));
        redisCache.deleteObject(redisCache.scan("queryPostUser-*-"+SecurityUtils.getUserId()));
        redisCache.deleteObject(redisCache.scan("queryUserPosts-*-"+SecurityUtils.getUserId()));
        redisCache.deleteObject(redisCache.scan("postTime-"+post.getSectionId()+"-"+post.getTagId()+"-*"));
        return ResponseResult.success(post.getId(),"");
    }

    @Log(title = "帖子管理",businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('user:posts:edit')")
    @PostMapping("/editPost")
    public ResponseResult editPost(@RequestBody Posts post){
        post.setUpdateBy(SecurityUtils.getUsername());
        post.setUpdateTime(new Date());
        postsService.updateById(post);
        redisCache.deleteObject(redisCache.scan("queryPost-"+ post.getId() +"-*"));
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
        redisCache.deleteObject(redisCache.scan("queryPost-"+ postId +"-*"));
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
        redisCache.deleteObject(redisCache.scan("queryPost-"+ postId +"-*"));
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
