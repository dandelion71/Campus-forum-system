package com.dandelion.system.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikesVo implements Serializable {
    public static final long serialVersionUID =1L;

    @TableId(value = "id",type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long postsId;
    private String isLike;
}
