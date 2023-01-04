package com.dandelion.system.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.dandelion.system.dao.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDynamic {
    private Long postId;
    private String content;
    private Long parentId;
    private Long targetUserId;
    private Date createTime;
    private String isPost;

    @TableField(select = false)
    private PostsSimpleVo post;
    @TableField(select = false)
    private String parentContent;
    @TableField(select = false)
    private UserVo targetUser;

}
