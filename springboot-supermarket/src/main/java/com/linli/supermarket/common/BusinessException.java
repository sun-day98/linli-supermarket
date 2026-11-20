package com.linli.supermarket.common;

/**
 * 业务异常。抛出后由 {@link GlobalExceptionHandler} 统一转成 Result，并触发事务回滚。
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码。 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.PARAM_ERROR.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code == null ? ResultCode.PARAM_ERROR.getCode() : code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.code = resultCode.getCode();
    }

    public Integer getCode() {
        return code;
    }
}
