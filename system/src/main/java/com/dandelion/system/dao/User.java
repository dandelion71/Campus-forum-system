package com.dandelion.system.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dandelion.system.vo.RoleVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user")
public class User implements Serializable {
    public static final long serialVersionUID =1L;
    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String userName;
    @TableField(select = false)
    private String password;
    private String email;
    private String phonenumber;
    private String sex;
    private String avatar;
    private String status;
    private String delFlag;
    private String loginIp;
    private Date loginDate;
    private Date pwdUpdateDate;
    private String updateBy;
    private Date updateTime;
    private Date createTime;
    private String muted;

    @TableField(select = false)
    private RoleVo role;
}
