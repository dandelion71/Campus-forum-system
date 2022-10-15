package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Section;
import com.dandelion.system.dao.Tag;
import com.dandelion.system.vo.SectionVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SectionMapper extends BaseMapper<Section> {

    void insertSectionTag(String sectionId,String tagId);

    SectionVo getSectionVoById(Long id);

    List<Tag> selectHaveTagBySectionId(String sectionId);

    List<Tag> selectNoneTagBySectionId(String sectionId);


}
