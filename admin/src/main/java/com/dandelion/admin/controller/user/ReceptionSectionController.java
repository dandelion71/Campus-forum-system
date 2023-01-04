package com.dandelion.admin.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Section;
import com.dandelion.system.mapper.CommentMapper;
import com.dandelion.system.mapper.PostsMapper;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.SectionService;
import com.dandelion.system.vo.SectionMasterVo;
import com.dandelion.system.vo.SectionVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/reception/section")
public class ReceptionSectionController {
    @Autowired
    private PostsService postsService;

    @Autowired
    private PostsMapper postsMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SectionMapper sectionMapper;

    @Autowired
    private SectionService sectionService;

    @ApiOperation(value = "主页顶部菜单")
    @GetMapping("/queryTopSection")
    public ResponseResult queryTopSection(){
        return ResponseResult.success(sectionMapper.selectTopSection());
    }

    //    @ApiOperation(value = "分区查询",notes = "查询分区以及分区下的版块")
    @GetMapping("/queryAll")
    public ResponseResult queryAll(){
        List<SectionMasterVo> sectionMasterVos = sectionMapper.selectSection(
                new QueryWrapper<SectionMasterVo>()
                        .eq("parent_id","0")
                        .ne("status","1"));
        for (SectionMasterVo sectionMasterVo : sectionMasterVos) {
            sectionMasterVo.setChildren(sectionMapper.selectSection(
                    new QueryWrapper<SectionMasterVo>()
                            .eq("parent_id", sectionMasterVo.getId())
                            .ne("status","1")));

            for (SectionMasterVo child : sectionMasterVo.getChildren()) {
                child.setAllPostNum(postsService.count(
                        new LambdaQueryWrapper<Posts>()
                                .eq(Posts::getSectionId,child.getId()).eq(Posts::getDelFlag,0)));
                child.setTodayPostNum(postsMapper.selectTodayPostNums(child.getId()));
                child.setTodayPostComment(commentMapper.selectTodayPostComment(child.getId()));
            }
        }
        return ResponseResult.success(sectionMasterVos, Massage.SELECT.value());
    }

    @GetMapping("/queryOne/{sectionId}")
    public ResponseResult queryOne(@PathVariable String sectionId){
        SectionVo sectionVo = sectionMapper.selectSectionById(sectionId);
        sectionVo.setAllPostNum(postsService.count(
                new LambdaQueryWrapper<Posts>()
                        .eq(Posts::getSectionId,sectionVo.getId()).eq(Posts::getDelFlag,0)));
        sectionVo.setTodayPostNum(postsMapper.selectTodayPostNums(sectionVo.getId()));
        sectionVo.setTodayPostComment(commentMapper.selectTodayPostComment(sectionVo.getId()));
        return ResponseResult.success(sectionVo, Massage.SELECT.value());
    }

    @GetMapping("/queryNotice/{sectionId}")
    public ResponseResult queryNotice(@PathVariable String sectionId){
        return ResponseResult.success(sectionMapper.selectById(sectionId).getNotice(),"");
    }

    @GetMapping("/queryModerator/{sectionId}")
    public ResponseResult queryModerator(@PathVariable String sectionId){
        return ResponseResult.success(sectionMapper.getSectionModerator(sectionId));
    }

    @Log(title = "版块管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('moderator:section:edit')")
    @PostMapping("/updateNotice")
    public ResponseResult updateNotice(@RequestBody Section section){
        section.setUpdateBy(SecurityUtils.getUsername());
        section.setCreateTime(new Date());
        return ResponseResult.success(sectionService.updateById(section));
    }
}
