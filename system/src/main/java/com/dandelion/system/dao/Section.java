package com.dandelion.system.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("section")
public class Section implements Serializable {
    public static final long serialVersionUID =1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String sectionName;
    private String title;
    private Long parentId;
    private Integer orderNum;
    private String icon;
    private String notice;
    private String status;
    private String isDel;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String remark;

    @TableField(select = false)
    private List<Section> section;
}
