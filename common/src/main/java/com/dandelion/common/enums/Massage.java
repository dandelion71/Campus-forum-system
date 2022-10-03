package com.dandelion.common.enums;

import org.springframework.http.HttpStatus;

/**
 * 业务操作成功提示信息
 */
public enum Massage {

    SAVE("添加成功"),

    DELETE("删除成功"),

    UPDATE("修改成功"),

    SELECT("查询成功");

    private final String value;

    Massage(String value) {
        this.value=value;
    }

    public String value(){
        return this.value;
    }
}
