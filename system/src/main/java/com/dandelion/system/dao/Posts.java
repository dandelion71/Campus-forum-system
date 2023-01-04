package com.dandelion.system.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dandelion.system.vo.SectionMasterVo;
import com.dandelion.system.vo.TagVo;
import com.dandelion.system.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("posts")
public class Posts implements Serializable {
    public static final long serialVersionUID =1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sectionId;
    private String title;
    private Long tagId;
    private String content;
    private Integer seeNum;
    private Integer likesNum;
    private Integer collectionNum;
    private String top;
    private String elite;
    private String status;
    private String delFlag;
    private String updateBy;
    private Date updateTime;
    private Date createTime;
    private String updateCause;

    @TableField(select = false)
    private UserVo user;
    @TableField(select = false)
    private SectionMasterVo section;
    @TableField(select = false)
    private TagVo tag;
    @TableField(select = false)
    private Boolean isUserLike;
    @TableField(select = false)
    private Boolean isUserCollection;
    @TableField(select = false)
    private Boolean isEditPost;
}
