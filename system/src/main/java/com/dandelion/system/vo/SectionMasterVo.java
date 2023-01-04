package com.dandelion.system.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionMasterVo implements Serializable {
    public static final long serialVersionUID =1L;

    private Long id;
    private String sectionName;
    private String title;
    private String icon;
    private String status;

    @TableField(select = false)
    private List<SectionMasterVo> children;
    @TableField(select = false)
    private Long allPostNum;
    @TableField(select = false)
    private Long todayPostNum;
    @TableField(select = false)
    private Long todayPostComment;
}
