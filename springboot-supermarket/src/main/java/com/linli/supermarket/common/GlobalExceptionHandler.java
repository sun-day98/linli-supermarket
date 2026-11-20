package com.linli.supermarket.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：所有异常统一包装成 {@link Result}，HTTP 状态保持 200，由业务 code 表达结果。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常。 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("业务异常 [{}] {} -> {}", ex.getCode(), request.getRequestURI(), ex.getMessage());
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    /** @Valid 请求体校验失败。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR, message.isEmpty() ? "参数错误" : message);
    }

    /** @Validated 方法参数校验失败。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(ResultCode.PARAM_ERROR, message.isEmpty() ? "参数错误" : message);
    }

    /** 请求体 JSON 格式错误。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException ex) {
        return Result.fail(ResultCode.PARAM_ERROR, "请求体格式错误");
    }

    /** 缺少必填查询参数。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException ex) {
        return Result.fail(ResultCode.PARAM_ERROR, "缺少参数：" + ex.getParameterName());
    }

    /** 参数类型不匹配。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return Result.fail(ResultCode.PARAM_ERROR, "参数格式错误：" + ex.getName());
    }

    /** 兜底异常。 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error("系统异常 [{}]", request.getRequestURI(), ex);
        return Result.fail(ResultCode.INTERNAL_ERROR);
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ":" + error.getDefaultMessage();
    }
}
