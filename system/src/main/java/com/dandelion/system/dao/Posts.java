package com.dandelion.system.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("posts")
public class Posts {
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
    private String delFlag;
    private String elite;
    private String updateBy;
    private Date updateTime;
    private Date createTime;
}
