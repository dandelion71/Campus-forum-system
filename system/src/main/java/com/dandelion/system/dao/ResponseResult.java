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

    private static ResponseResult success(int code,String msg,Object data){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    private static ResponseResult success(int code,String msg){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
    private static ResponseResult success(int code,Object data){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        r.setData(data);
        return r;
    }

    private static ResponseResult success(int code){
        ResponseResult r = new ResponseResult();
        r.setCode(code);
        return r;
    }

    public static ResponseResult success(Object data,String msg){
        return success(HttpStatus.OK.value(),msg,data);
    }

    public static ResponseResult success(String msg){
        return success(HttpStatus.OK.value(),msg);
    }

    public static ResponseResult success(Object data){
        return success(HttpStatus.OK.value(),data);
    }

    public static ResponseResult success(){
        return success(HttpStatus.OK.value());
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
