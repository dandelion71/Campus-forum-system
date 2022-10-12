package com.dandelion.admin.controller.system;

import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.service.PostsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/posts")
public class PostsController {

    @Autowired
    private PostsService postsService;

    @ApiOperation(value = "帖子管理")
    @PreAuthorize("@dandelion.hasAuthority('system:posts:list')")
    @GetMapping("/list")
    public ResponseResult list(){
        return null;
    }
}
