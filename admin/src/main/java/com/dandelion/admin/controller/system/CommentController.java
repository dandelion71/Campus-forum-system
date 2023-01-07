package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.UserMapper;
import com.dandelion.system.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserMapper userMapper;

    @PreAuthorize("@dandelion.hasAuthority('system:comment:query')")
    @GetMapping("/list")
    public ResponseResult queryUser(@RequestParam(defaultValue = "1") Integer currentPage,
                                    @RequestParam(defaultValue = "5") Integer pageSize,
                                    @RequestParam(defaultValue = "0") String key,
                                    @RequestParam(defaultValue = "0") String value) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        switch (key){
            case "0":break;
            case "1":queryWrapper.eq(Comment::getUserId,value);break;
            case "2":queryWrapper.eq(Comment::getTargetUserId,value);break;
            case "3":queryWrapper.like(Comment::getContent,value);break;
            case "4":queryWrapper.eq(Comment::getPostId,value);break;
        }
        Page<Comment> commentPage = new Page<>(currentPage, pageSize);
        IPage<Comment> page = commentService.page(commentPage,queryWrapper.orderByDesc(Comment::getCreateTime));
        List<Comment> comments = page.getRecords();
        for (Comment comment : comments) {
            comment.setUser(userMapper.getUserVoById(comment.getUserId()));
            comment.setTargetUser(userMapper.getUserVoById(comment.getTargetUserId()));
        }
        return ResponseResult.success(page);
    }

    @Log(title = "评论管理",businessType = BusinessType.DELETE)
    @PostMapping("/remove/{id}")
    @PreAuthorize("@dandelion.hasAuthority('system:comment:remove')")
    public ResponseResult remove(@PathVariable String id){
        commentService.update(new LambdaUpdateWrapper<Comment>().eq(Comment::getId,id).set(Comment::getDelFlag,2));
        return ResponseResult.success(Massage.DELETE.value());
    }
}
