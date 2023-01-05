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

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("authentication")
public class Authentication implements Serializable {
    public static final long serialVersionUID =1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String onlineCode;
    private String studentCard;
    private String refuse;
    private Date expire;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String pass;

    @TableField(select = false)
    private UserVo user;
    @TableField(select = false)
    private String status;
}
