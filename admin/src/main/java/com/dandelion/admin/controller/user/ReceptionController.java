package com.dandelion.admin.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.common.utils.StringUtils;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.CommentService;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisCache redisCache;


    @GetMapping("/isModerator/{sectionId}")
    @PreAuthorize("@dandelion.hasAuthority('user:user:edit')")
    public ResponseResult isModerator(@PathVariable String sectionId){
        String key = "isModerator-"+sectionId+"-"+SecurityUtils.getUserId() ;
        Boolean isModerator = redisCache.getCacheObject(key);
        if (StringUtils.isNull(isModerator)){
            if(SecurityUtils.isAdmin(SecurityUtils.getUserId())){
                redisCache.setCacheObject(key,true,24, TimeUnit.HOURS);
                return ResponseResult.success(true,"");
            }
            String sectionUser=sectionMapper.getSectionUser(SecurityUtils.getUserId(),sectionId);
            isModerator = StringUtils.isNotEmpty(sectionUser);
            redisCache.setCacheObject(key,isModerator,24, TimeUnit.HOURS);
        }
        return ResponseResult.success(isModerator,"");
    }

    @GetMapping("/isAdmin")
    @PreAuthorize("@dandelion.hasAuthority('user:user:edit')")
    public ResponseResult isAdmin(){
        String key = "isAdmin-"+SecurityUtils.getUserId() ;
        Boolean isAdmin = redisCache.getCacheObject(key);
        if (StringUtils.isNull(isAdmin)){
            isAdmin = SecurityUtils.isAdmin(SecurityUtils.getUserId());
            redisCache.setCacheObject(key,isAdmin,365, TimeUnit.DAYS);
        }
        return ResponseResult.success(isAdmin,"");
    }


    @GetMapping("/topNums")
    public ResponseResult queryNums(){
        String key = "topNums";
        Map<String, Long> map = redisCache.getCacheMap(key);
        if (StringUtils.isEmpty(map)){
            long userCount = userService.count();
            long commentCount = commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, 0).eq(Comment::getDelFlag,0));
            long postCount = postsService.count(new LambdaQueryWrapper<Posts>().eq(Posts::getDelFlag,0));
            map.put("userCount",userCount);
            map.put("commentCount",commentCount);
            map.put("postCount",postCount);
            redisCache.setCacheMap(key,map);
            redisCache.expire(key,60);
        }
        return ResponseResult.success(map,null);
    }




}
