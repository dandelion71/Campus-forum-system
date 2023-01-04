package com.dandelion.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dandelion.system.dao.Menu;
import com.dandelion.system.dao.Posts;
import com.dandelion.system.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostsMapper extends BaseMapper<Posts> {
    Long selectTodayPostNums(Long sectionId);

    List<PostsSimpleVo> selectNewPost();
    List<PostsSimpleVo> selectNewElitePost();
    List<PostsSimpleVo> selectNewPostComment();
    List<PostsSimpleVo> selectHotPost(String sectionId);
    IPage<PostsVo> selectDefaultPost(String sectionId, String tagId, Page<PostsVo> page);
    IPage<PostsVo> selectPostByTabs(Page<PostsVo> page,
                                    @Param(Constants.WRAPPER) Wrapper<PostsVo> queryWrapper,
                                    @Param("sectionId") String sectionId,
                                    @Param("tagId") String tagId);

    IPage<PostsVo> selectAllPostByUser(Page<PostsVo> page,
                                    @Param(Constants.WRAPPER) Wrapper<PostsVo> queryWrapper,String userId);

    IPage<PostsVo> selectElitePostByUser(Page<PostsVo> page,
                                       @Param(Constants.WRAPPER) Wrapper<PostsVo> queryWrapper,String userId);

    IPage<PostsVo> selectPostByUserCollection(Page<PostsVo> page,
                                         @Param(Constants.WRAPPER) Wrapper<PostsVo> queryWrapper,String userId);

    LikesVo selectLikes(String postId, String userId);

    boolean insertLikes(String postId, String userId);

    boolean updateLikes(String postId, String userId,String isLike);

    Long selectLikesNum(String postId);

    CollectionVo selectCollection(String postId, String userId);

    boolean insertCollection(String postId, String userId);

    boolean updateCollection(String postId, String userId,String isCollection);

    Long selectCollectionNum(String postId);

    PostsUpdateVo selectPostUpdateVo(String postId);

    PostsSimpleVo selectPostsSimpleVo(String postId);
}
