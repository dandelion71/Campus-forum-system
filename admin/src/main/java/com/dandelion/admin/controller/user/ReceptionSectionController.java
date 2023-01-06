package com.dandelion.admin.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dandelion.common.annotation.Log;
import com.dandelion.common.enums.BusinessType;
import com.dandelion.common.enums.Massage;
import com.dandelion.common.utils.RedisCache;
import com.dandelion.common.utils.SecurityUtils;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.dao.Section;
import com.dandelion.system.mapper.CommentMapper;
import com.dandelion.system.mapper.PostsMapper;
import com.dandelion.system.mapper.SectionMapper;
import com.dandelion.system.service.PostsService;
import com.dandelion.system.service.SectionService;
import com.dandelion.system.vo.PostsSimpleVo;
import com.dandelion.system.vo.SectionMasterVo;
import com.dandelion.system.vo.SectionVo;
import com.dandelion.system.vo.UserVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisCache redisCache;

    @ApiOperation(value = "主页顶部菜单")
    @GetMapping("/queryTopSection")
    public ResponseResult queryTopSection(){
        String key = "queryTopSection";
        Map<String, List<SectionMasterVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",sectionMapper.selectTopSection());
            redisCache.setCacheMap(key,map,1, TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryAll")
    public ResponseResult queryAll(){
        String key = "queryAllSection";
        Map<String, List<SectionMasterVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
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
            map.put("list",sectionMasterVos);
            redisCache.setCacheMap(key,map,5, TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @GetMapping("/queryOne/{sectionId}")
    public ResponseResult queryOne(@PathVariable String sectionId){
        String key = "querySection-"+sectionId;
        Map<String, SectionVo> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            SectionVo sectionVo = sectionMapper.selectSectionById(sectionId);
            sectionVo.setAllPostNum(postsService.count(
                    new LambdaQueryWrapper<Posts>()
                            .eq(Posts::getSectionId,sectionVo.getId()).eq(Posts::getDelFlag,0)));
            sectionVo.setTodayPostNum(postsMapper.selectTodayPostNums(sectionVo.getId()));
            sectionVo.setTodayPostComment(commentMapper.selectTodayPostComment(sectionVo.getId()));
            map.put("bean",sectionVo);
            redisCache.setCacheMap(key,map,5, TimeUnit.MINUTES);
        }
        return ResponseResult.success(map.get("bean"));
    }

    @GetMapping("/queryNotice/{sectionId}")
    public ResponseResult queryNotice(@PathVariable String sectionId){
        String key = "queryNotice-"+sectionId;
        Map<String, String> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("value",sectionMapper.selectById(sectionId).getNotice());
            redisCache.setCacheMap(key,map,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("value"),"");
    }

    @GetMapping("/queryModerator/{sectionId}")
    public ResponseResult queryModerator(@PathVariable String sectionId){
        String key = "queryModerator-"+sectionId;
        Map<String, List<UserVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",sectionMapper.getSectionModerator(sectionId));
            redisCache.setCacheMap(key,map,7,TimeUnit.DAYS);
        }
        return ResponseResult.success(map.get("list"));
    }

    @Log(title = "版块管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@dandelion.hasAuthority('moderator:section:edit')")
    @PostMapping("/updateNotice")
    public ResponseResult updateNotice(@RequestBody Section section){
        section.setUpdateBy(SecurityUtils.getUsername());
        section.setCreateTime(new Date());
        redisCache.deleteObject("queryNotice-"+section.getId());
        redisCache.deleteObject("querySection-"+section.getId());
        return ResponseResult.success(sectionService.updateById(section));
    }
}
