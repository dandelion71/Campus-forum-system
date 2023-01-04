package com.dandelion.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostsSimpleVo implements Serializable {
    public static final long serialVersionUID =1L;

    private Long id;
    private Long sectionId;
    private String title;
    private String createTime;

}
