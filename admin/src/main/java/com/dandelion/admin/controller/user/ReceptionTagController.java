package com.dandelion.admin.controller.user;

import com.dandelion.common.enums.Massage;
import com.dandelion.system.dao.ResponseResult;
import com.dandelion.system.mapper.TagMapper;
import com.dandelion.system.service.CommentService;
import com.dandelion.system.service.TagService;
import com.dandelion.system.vo.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/reception/tag")
public class ReceptionTagController {
    @Autowired
    private TagMapper tagMapper;

    @GetMapping("/querySectionVo/{sectionId}")
    public ResponseResult querySectionVo(@PathVariable String sectionId){
        return ResponseResult.success(tagMapper.getSectionVo(sectionId), Massage.SELECT.value());
    }
}
