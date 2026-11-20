package com.linli.supermarket.common;

import java.io.Serializable;

/**
 * 全局统一响应包装：{@code {"code":0,"data":...,"message":"ok"}}。
 *
 * @param <T> 业务数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，0 表示成功。 */
    private Integer code;

    /** 业务数据。 */
    private T data;

    /** 提示信息。 */
    private String message;

    public Result() {
    }

    public Result(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /** 返回一个成功响应（无数据）。 */
    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), null, ResultCode.SUCCESS.getMessage());
    }

    /** 返回一个成功响应（带数据）。 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), data, ResultCode.SUCCESS.getMessage());
    }

    /** 返回一个成功响应（带数据和自定义消息）。 */
    public static <T> Result<T> ok(T data, String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), data, message);
    }

    /** 返回一个失败响应。 */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, null, message);
    }

    /** 返回一个失败响应（使用统一错误码的默认消息）。 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), null, resultCode.getMessage());
    }

    /** 返回一个失败响应（使用统一错误码并覆盖消息）。 */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), null, message);
    }

    /** 业务是否成功。 */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
