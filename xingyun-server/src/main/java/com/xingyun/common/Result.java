package com.xingyun.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结构。
 * <pre>
 *   code：状态码，200 成功，其他为各类失败
 *   msg ：提示信息，前端可直接弹给用户看
 *   data：真正的业务数据
 * </pre>
 *
 * @author xingyun
 */
@Data
public class Result<T> implements Serializable {

    public static final int CODE_SUCCESS = 200;
    /** 业务/参数类错误：由客户端请求触发，区别于真正的服务端故障 */
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_ERROR = 500;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_FORBIDDEN = 403;

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /** 成功（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    /** 成功（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    /** 成功（自定义提示 + 数据） */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(CODE_SUCCESS, msg, data);
    }

    /** 失败（自定义提示） */
    public static <T> Result<T> error(String msg) {
        return new Result<>(CODE_ERROR, msg, null);
    }

    /** 失败（自定义状态码 + 提示） */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
