package com.dandelion.system.dao;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@TableName("sys_menu")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Menu implements Serializable {
  public static final long serialVersionUID =1L;

  @TableId(value = "id",type = IdType.AUTO)
  private Long id;
  private String menuName;
  private Long parentId;
  private Integer orderNum;
  private String path;
  private String component;
  private String menuType;
  private String visible;
  private String status;
  private String perms;
  private String icon;
  private String createBy;
  private Date createTime;
  private String updateBy;
  private Date updateTime;
  private String remark;

}
