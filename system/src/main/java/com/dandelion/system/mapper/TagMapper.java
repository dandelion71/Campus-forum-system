package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Tag;
import com.dandelion.system.vo.TagVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    TagVo getTagVoById(Long id);

    List<Long> getSectionTag(String sectionId);

    void deleteSectionTagByTagId(String tagId);

    void updatePostsTagId(String oldTagId,String newTagId);
}
