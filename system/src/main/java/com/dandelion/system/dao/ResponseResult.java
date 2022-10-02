package com.dandelion.system.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult {
    private Integer code;

    private String msg;

    private Object data;


    public static ResponseResult success(Object data,int status){
        String msg;
        switch (status){
            case 1:msg="查询成功";break;
            case 2:msg="添加成功";break;
            case 3:msg="修改成功";break;
            case 4:msg="删除成功";break;
            default:msg=null;
        }
        return success(HttpStatus.OK.value(),msg,data);
    }

    public static ResponseResult success(int code,String msg,Object data){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    public static ResponseResult success(int code,String msg){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static ResponseResult success(String msg){
        return success(HttpStatus.BAD_REQUEST.value(),msg);
    }


    public static ResponseResult fail(int code,String msg,Object data){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    public static ResponseResult fail(int code,String msg){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static ResponseResult fail(String msg){
        return fail(HttpStatus.BAD_REQUEST.value(),msg);
    }
}
