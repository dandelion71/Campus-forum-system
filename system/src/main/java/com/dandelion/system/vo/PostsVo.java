package com.dandelion.system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostsVo implements Serializable {
    public static final long serialVersionUID =1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sectionId;
    private String title;
    private Long tagId;
    private Integer seeNum;
    private Integer likesNum;
    private Integer collectionNum;
    private Integer commentNum;
    private String top;
    private String delFlag;
    private String elite;
    private Date createTime;
    private Date commentCreateTime;

    @TableField(select = false)
    private UserVo user;
    @TableField(select = false)
    private SectionMasterVo section;
    @TableField(select = false)
    private TagVo tag;


}
