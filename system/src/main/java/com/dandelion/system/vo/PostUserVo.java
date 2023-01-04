package com.dandelion.system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostUserVo implements Serializable {
    public static final long serialVersionUID =1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private String userName;
    private String avatar;
    private String status;
    private Date createTime;
    private Date loginDate;
    private String muted;

    @TableField(select = false)
    private Long postNum;
    @TableField(select = false)
    private Long commentPostNum;
    @TableField(select = false)
    private Long elitePostNum;
    @TableField(select = false)
    private Long collectionPostNum;
    @TableField(select = false)
    private String role;
    @TableField(select = false)
    private Date mutedTime;
}
