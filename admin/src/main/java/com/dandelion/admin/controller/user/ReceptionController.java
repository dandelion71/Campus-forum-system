package com.dandelion.admin.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.CommentService;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.SectionService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reception")
public class ReceptionController {
    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostsService postsService;

    @Autowired
    private SectionMapper sectionMapper;


    @GetMapping("/isModerator/{sectionId}")
    public ResponseResult isModerator(@PathVariable String sectionId){
        if(SecurityUtils.isAdmin(SecurityUtils.getUserId())){
            return ResponseResult.success(true,"");
        }
        String sectionUser=sectionMapper.getSectionUser(SecurityUtils.getUserId(),sectionId);
        return ResponseResult.success(StringUtils.isNotEmpty(sectionUser),"");
    }

    @GetMapping("/isAdmin")
    public ResponseResult isAdmin(){
        return ResponseResult.success(SecurityUtils.isAdmin(SecurityUtils.getUserId()),"");
    }


    @GetMapping("/topNums")
    public ResponseResult queryNums(){
        long userCount = userService.count();
        long commentCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, 0).eq(Comment::getDelFlag,0));
        long postCount = postsService.count(new LambdaQueryWrapper<Posts>().eq(Posts::getDelFlag,0));
        Map<Object, Long> map = new HashMap<>();
        map.put("userCount",userCount);
        map.put("commentCount",commentCount);
        map.put("postCount",postCount);
        return ResponseResult.success(map,null);
    }




}
