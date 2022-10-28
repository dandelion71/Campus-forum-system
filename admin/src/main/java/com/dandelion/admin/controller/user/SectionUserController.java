package com.dandelion.admin.controller.user;

import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.SectionMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/section")
public class SectionUserController {

    @Autowired
    private SectionMapper sectionMapper;

    @ApiOperation(value = "主页顶部菜单")
    @GetMapping("/queryTopSection")
    public ResponseResult queryTopSection(){
        return ResponseResult.success(sectionMapper.selectTopSection());
    }
}
