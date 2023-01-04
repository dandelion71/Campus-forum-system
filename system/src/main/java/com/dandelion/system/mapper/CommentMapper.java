package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dandelion.system.dao.Comment;
import com.dandelion.system.vo.LikesVo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    Long selectTodayPostComment(Long sectionId);

    LikesVo selectLikes(String commentId, String userId);

    boolean insertLikes(String commentId, String userId);

    boolean updateLikes(String commentId, String userId,String isLike);

    Long selectLikesNum(String commentId);
}
