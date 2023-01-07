package com.dandelion.admin.controller.user;

import com.dandelion.common.utils.RedisCache;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.vo.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/reception/tag")
public class ReceptionTagController {
    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RedisCache redisCache;

    @GetMapping("/querySectionVo/{sectionId}")
    public ResponseResult querySectionVo(@PathVariable String sectionId){
        String key = "querySectionVo-"+sectionId;
        Map<String, List<TagVo>> map = redisCache.getCacheMap(key);
        if (!redisCache.existKey(key)){
            map.put("list",tagMapper.getSectionVo(sectionId));
            redisCache.setCacheMap(key,map);
        }
        return ResponseResult.success(map.get("list"));
    }
}
