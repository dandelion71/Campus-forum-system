package com.dandelion.admin.controller.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.User;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.CommentService;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/system")
public class SystemController {
    public static final Long oneDay=24*60*60*1000L;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostsService postsService;

    @Autowired
    private SectionMapper sectionMapper;


    @GetMapping("/todayNewUser")
    public ResponseResult todayNewUser(){
        Long[] date = getDate();
        Long[] num = new Long[5];
        for (int i = 0; i < date.length-1; i++) {
            num[i]=userService.count(new LambdaQueryWrapper<User>().between(User::getCreateTime, new Date(date[i]), new Date(date[i + 1] - 1)));
        }
        return ResponseResult.success(num);
    }

    @GetMapping("/authenticationNum")
    public ResponseResult authenticationNum(){
        Long[] num = new Long[3];
        for (int i = 0; i < num.length; i++) {
            num[i]=userService.count(new LambdaQueryWrapper<User>().eq(User::getStatus,i));
        }
        return ResponseResult.success(num);
    }

    @GetMapping("/todayNewPost")
    public ResponseResult todayNewPost(){
        Long[] date = getDate();
        Long[] num = new Long[5];
        for (int i = 0; i < date.length-1; i++) {
            num[i]=postsService.count(new LambdaQueryWrapper<Posts>().between(Posts::getCreateTime, new Date(date[i]), new Date(date[i + 1] - 1)));
        }
        return ResponseResult.success(num);
    }

    @GetMapping("/todayNewComment")
    public ResponseResult todayNewComment(){
        Long[] date = getDate();
        Long[] num = new Long[5];
        for (int i = 0; i < date.length-1; i++) {
            num[i]=commentService.count(new LambdaQueryWrapper<Comment>().between(Comment::getCreateTime, new Date(date[i]), new Date(date[i + 1] - 1)));
        }
        return ResponseResult.success(num);
    }

    private Long[] getDate(){
        Long[] dates = new Long[6];
        Date nowDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = -4, j=0 ;i <=1 ; i++,j++) {
            Date date = new Date(nowDate.getTime() + i * oneDay);
            try {
                dates[j]=format.parse(format.format(date)).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return dates;
    }
}
