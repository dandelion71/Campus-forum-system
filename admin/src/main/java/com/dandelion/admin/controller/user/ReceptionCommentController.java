package com.dandelion.admin.controller.user;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.CommentMapper;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.CommentService;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.vo.LikesVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/reception/comment")
public class ReceptionCommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostsService postsService;

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/queryUpdateComment/{commentId}")
    public ResponseResult queryUpdateComment(@PathVariable String commentId){
        String content = commentService.getObj(new LambdaQueryWrapper<Comment>().select(Comment::getContent).eq(Comment::getId, commentId), Object::toString);
        return ResponseResult.success(content,"");
    }

    @GetMapping("/queryHotComment/{postId}")
    public ResponseResult queryHotComment(@PathVariable String postId){
        String key = "HotComment-"+postId;
        boolean flag = redisCache.existKey(key);
        Map<String, List<Comment>> map = redisCache.getCacheMap(key);
        if (!flag){
            List<Comment> commentList = commentService.list(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getPostId, postId)
                    .eq(Comment::getParentId, 0)
                    .gt(Comment::getLikesNum, 10)
                    .orderByDesc(Comment::getLikesNum));
            setChildrenComment(commentList,0,0);
            map.put("list", commentList);
            redisCache.setCacheMap(key,map);
            redisCache.expire(key,30, TimeUnit.SECONDS);
        }
        return ResponseResult.success(map.get("list"));
    }


    @GetMapping("/queryComment/{postId}")
    public ResponseResult queryComment(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @PathVariable String postId) {
        String key = "queryCommentPage-"+postId+"-"+currentPage;
        boolean flag = redisCache.existKey(key);
        Map<String,IPage<Comment>> map = redisCache.getCacheMap(key);
        if(!flag){
            Page<Comment> commentPage = new Page<>(currentPage, pageSize);
            IPage<Comment> page = commentService.page(commentPage,new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getPostId,postId)
                    .eq(Comment::getParentId,0)
                    .orderByAsc(Comment::getCreateTime));
            List<Comment> comments = page.getRecords();
            setChildrenComment(comments,currentPage,pageSize);
            map.put("page",page);
            redisCache.setCacheMap(key,map);
            redisCache.expire(key,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("page"));
    }

    @GetMapping("/queryChildrenComment/{postId}/{parentId}")
    public ResponseResult queryChildrenComment(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "5") Integer pageSize,
                                               @PathVariable String postId, @PathVariable String parentId) {
        String key = "queryChildrenComment-"+postId+"-"+parentId+"-"+currentPage;
        boolean flag = redisCache.existKey(key);
        Map<String,IPage<Comment>> map = redisCache.getCacheMap(key);
        if(!flag){
            Page<Comment> childrenCommentPage = new Page<>(currentPage, pageSize);
            IPage<Comment> page = commentService.page(childrenCommentPage,new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getPostId,postId)
                    .eq(Comment::getParentId,parentId)
                    .orderByAsc(Comment::getCreateTime));
            if (page.getTotal()==0){
                page=null;
            }else {
                List<Comment> childrenComment= page.getRecords();
                Object principal = SecurityUtils.getAuthentication().getPrincipal();
                for (Comment comment : childrenComment) {
                    if ("anonymousUser".equals(principal)){
                        comment.setIsEdit(false);
                    }else {
                        if(SecurityUtils.isAdmin(SecurityUtils.getUserId())){
                            comment.setIsEdit(true);
                        }else comment.setIsEdit(SecurityUtils.getUserId().equals(comment.getUserId()));
                    }
                    comment.setUser(userMapper.getUserVoById(comment.getUserId()));
                    comment.setTargetUser(userMapper.getUserVoById(comment.getTargetUserId()));
                }
            }
            map.put("page",page);
            redisCache.setCacheMap(key,map);
            redisCache.expire(key,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("page"));
    }

    @PreAuthorize("@dandelion.hasAuthority('user:comment:likes')")
    @PostMapping("/addLikes/{commentId}")
    public ResponseResult addLikes(@RequestParam Map<String,String> map,@PathVariable String commentId){
        String userId = SecurityUtils.getUserId().toString();
        LikesVo likes = commentMapper.selectLikes(commentId,userId);
        if (Objects.isNull(likes)){
            commentMapper.insertLikes(commentId,userId);
        }else {
            if (likes.getIsLike().equals("0")){
                commentMapper.updateLikes(commentId,userId,"1");
            }else {
                commentMapper.updateLikes(commentId,userId,"0");
            }
        }
        Long likesNum = commentMapper.selectLikesNum(commentId);
        commentService.update(new LambdaUpdateWrapper<Comment>().eq(Comment::getId,commentId).set(Comment::getLikesNum,likesNum));
        String postId = commentService.getObj(new LambdaQueryWrapper<Comment>().select(Comment::getPostId).eq(Comment::getId,commentId),Object::toString);
        String currentPage = map.get("currentPage");
        redisCache.deleteObject("queryCommentPage-"+postId+"-"+currentPage);
        return ResponseResult.success(likesNum);
    }

    @Log(title = "评论管理",businessType = BusinessType.UPDATE)
    @PostMapping("/editComment")
    @PreAuthorize("@dandelion.hasAuthority('user:comment:edit')")
    public ResponseResult editComment(@RequestParam Map<String,String> map,@RequestBody Comment comment){
        comment.setUpdateBy(SecurityUtils.getUsername());
        comment.setUpdateTime(new Date());
        commentService.updateById(comment);
        redisCache.deleteObject("queryNewPostComment");
        String currentPage= map.get("currentPage");
        redisCache.deleteObject("queryCommentPage-"+comment.getPostId()+"-"+currentPage);
        Posts posts = postsService.getById(comment.getPostId());
        redisCache.deleteObject(redisCache.scan("commentTime-"+posts.getSectionId()+"-0-*"));
        redisCache.deleteObject(redisCache.scan("commentTime-"+posts.getSectionId()+"-"+posts.getTagId()+"-*"));
        return ResponseResult.success();
    }

    @PreAuthorize("@dandelion.hasAuthority('user:comment:add')")
    @PostMapping("/saveComment")
    public ResponseResult saveComment(@RequestParam Map<String,String> map,@RequestBody Comment comment){
        comment.setCreateTime(new Date());
        comment.setUserId(SecurityUtils.getUserId());
        commentService.save(comment);
        redisCache.deleteObject("topNums");
        redisCache.deleteObject("queryNewPostComment");
        redisCache.deleteObject("querySectionById");
        String currentPage= map.get("currentPage");
        redisCache.deleteObject("queryCommentPage-"+comment.getPostId()+"-"+currentPage);
        Posts posts = postsService.getById(comment.getPostId());
        redisCache.deleteObject(redisCache.scan("commentTime-"+posts.getSectionId()+"-0-*"));
        redisCache.deleteObject(redisCache.scan("queryPostUser-*-"+SecurityUtils.getUserId()));
        redisCache.deleteObject(redisCache.scan("commentTime-"+posts.getSectionId()+"-"+posts.getTagId()+"-*"));
        return ResponseResult.success();
    }

    private void setChildrenComment(List<Comment> comments,Integer currentPage,Integer pageSize){
        int i = (currentPage - 1) * pageSize + 1;
        Object principal = SecurityUtils.getAuthentication().getPrincipal();
        for (Comment comment : comments) {
            comment.setUser(userMapper.getUserVoById(comment.getUserId()));
            comment.setLevel(i++);
            if ("anonymousUser".equals(principal)){
                comment.setIsUserLike(true);
                comment.setIsEdit(false);
            }else {
                LikesVo likes = commentMapper.selectLikes(comment.getId().toString(), SecurityUtils.getUserId().toString());
                if (Objects.isNull(likes)){
                    comment.setIsUserLike(true);
                }else {
                    comment.setIsUserLike(likes.getIsLike().equals("0"));
                }
                if(SecurityUtils.isAdmin(SecurityUtils.getUserId())){
                    comment.setIsEdit(true);
                }else comment.setIsEdit(SecurityUtils.getUserId().equals(comment.getUserId()));
            }
            Object data = queryChildrenComment(1, 5, comment.getPostId().toString(), comment.getId().toString()).getData();
            comment.setChildrenCommentPage((IPage<Comment>) data);
        }
    }
}
