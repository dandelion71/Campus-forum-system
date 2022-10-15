package com.dandelion.system.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dandelion.system.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment")
public class Comment implements Serializable {
    public static final long serialVersionUID =1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId;
    private Long targetUserId;
    private String content;
    private Integer likesNum;
    private String delFlag;
    private String updateBy;
    private Date updateTime;
    private Date createTime;

    @TableField(select = false)
    private UserVo user;
    @TableField(select = false)
    private UserVo targetUser;
    @TableField(select = false)
    private List<Comment> commentList;
}
