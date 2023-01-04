package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.dandelion.system.dao.Section;
import com.dandelion.system.dao.Tag;
import com.dandelion.system.vo.SectionMasterVo;
import com.dandelion.system.vo.SectionVo;
import com.dandelion.system.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SectionMapper extends BaseMapper<Section> {

    void insertSectionTag(String sectionId,String tagId);

    void insertSectionUser(String sectionId,String userId);

    void delSectionUser(String sectionId,String userId);

    void delSectionTag(String sectionId,String tagId);

    SectionMasterVo getSectionVoById(Long id);

    List<UserVo> getSectionModerator(String id);

    List<Tag> selectHaveTagBySectionId(String sectionId);

    List<Tag> selectNoneTagBySectionId(String sectionId);

    List<SectionMasterVo> selectTopSection();

    List<SectionMasterVo> selectSection(@Param(Constants.WRAPPER) Wrapper<SectionMasterVo> queryWrapper);

    SectionVo selectSectionById(String sectionId);

    String getSectionUser(Long userId,String sectionId);
}
