package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/system/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;


    @ApiOperation(value = "评论查询",notes = "根据 用户名 查询评论以及回复")
    @PreAuthorize("@dandelion.hasAuthority('system:comment:query')")
    @GetMapping("/queryUser/{userName}")
    public ResponseResult queryUser(@RequestParam(defaultValue = "1") Integer currentPage,
                               @RequestParam(defaultValue = "10") Integer pageSize,
                               @PathVariable String userName) {
        String userId = userMapper.getIdByUserName(userName);
        Assert.notNull(userId,"用户不存在");
        Page<Comment> commentPage = new Page<>(currentPage, pageSize);
        IPage<Comment> page = commentService.page(commentPage,new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId,userId)
                .ne(Comment::getDelFlag,2)
                .orderByDesc(Comment::getCreateTime));
        List<Comment> comments = page.getRecords();
        for (Comment comment : comments) {
            comment.setUser(userMapper.getUserVoById(comment.getUserId()));
            comment.setTargetUser(userMapper.getUserVoById(comment.getTargetUserId()));
        }
        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @ApiOperation(value = "评论查询",notes = "根据 postId parentId 查询评论以及回复")
    @PreAuthorize("@dandelion.hasAuthority('system:comment:query')")
    @GetMapping("/queryComment/{postId}/{parentId}")
    public ResponseResult queryComment(@RequestParam(defaultValue = "1") Integer currentPage,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @PathVariable String postId,
                                       @PathVariable String parentId) {
        Page<Comment> commentPage = new Page<>(currentPage, pageSize);
        IPage<Comment> page = commentService.page(commentPage,new LambdaQueryWrapper<Comment>()
                .eq(Comment::getPostId,postId)
                .eq(Comment::getParentId,parentId)
                .ne(Comment::getDelFlag,2)
                .orderByDesc(Comment::getCreateTime));
        List<Comment> comments = page.getRecords();
        for (Comment comment : comments) {
            comment.setUser(userMapper.getUserVoById(comment.getUserId()));
            if (!"0".equals(parentId)){
                List<Comment> commentList = commentService.list(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, comment.getId()));
                for (Comment c : commentList) {
                    c.setUser(userMapper.getUserVoById(c.getUserId()));
                    c.setTargetUser(userMapper.getUserVoById(c.getTargetUserId()));
                }
                comment.setCommentList(commentList);
            }
        }

        return ResponseResult.success(page, Massage.SELECT.value());
    }

    @ApiOperation(value = "评论修改")
    @Log(title = "评论管理",businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @PreAuthorize("@dandelion.hasAuthority('system:comment:edit')")
    public ResponseResult edit(@RequestBody Comment comment){
        comment.setUpdateBy(SecurityUtils.getUsername());
        comment.setUpdateTime(new Date());
        commentService.updateById(comment);
        return ResponseResult.success(Massage.UPDATE.value());
    }

    @ApiOperation(value = "评论删除")
    @Log(title = "评论管理",businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:comment:remove')")
    public ResponseResult remove(@PathVariable String id){
        commentService.update(new LambdaUpdateWrapper<Comment>().eq(Comment::getId,id).set(Comment::getDelFlag,2));
        return ResponseResult.success(Massage.DELETE.value());
    }
}
