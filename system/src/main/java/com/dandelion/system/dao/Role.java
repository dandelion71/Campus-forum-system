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
@TableName("sys_Role")
public class Role {
    public static final long serialVersionUID =1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String roleName;
    private String roleKey;
    private String isDel;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String remark;

}
