package com.dandelion.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo implements Serializable {
    public static final long serialVersionUID =1L;

    private Long id;
    private String userName;
    private String avatar;
    private String status;
    private String delFlag;
    private String muted;
}
