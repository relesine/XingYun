package com.xingyun.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器：所有 Controller 抛出的异常在这里被统一拦截并转成标准 Result，
 * 避免把堆栈信息直接抛到前端（既不友好也不安全）。
 *
 * @author xingyun
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 1. 业务异常：把我们自己抛出的提示语原样返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 2. @RequestBody 参数校验失败（@Valid 注解的 DTO） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 3. 表单方式参数校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 4. 请求体 JSON 解析失败（少逗号、多引号等），返回 400 友好提示 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error(400, "请求参数格式错误，请检查 JSON 格式");
    }

    /** 5. 缺少必填的 URL 参数（如 @RequestParam 缺值） */
    @ExceptionHandler({MissingServletRequestParameterException.class, ServletRequestBindingException.class})
    public Result<Void> handleMissingParam(Exception e) {
        log.warn("参数绑定失败：{}", e.getMessage());
        return Result.error(400, "缺少必要的请求参数");
    }

    /** 6. 文件上传超过大小限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        log.warn("上传文件超过大小限制：{}", e.getMessage());
        return Result.error(400, "上传文件过大，请检查文件大小限制");
    }

    /** 7. 请求方法不支持（如把 POST 接口当 GET 调用） */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return Result.error(405, "请求方式不支持，请检查接口文档");
    }

    /** 8. 兜底异常：未知错误，记录完整堆栈，前端只看到友好提示 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(500, "系统繁忙，请稍后再试");
    }
}
