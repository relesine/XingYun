package com.xingyun.common;

import lombok.Getter;

/**
 * 自定义业务异常：业务校验不通过时抛出，由全局异常处理器统一转成 Result 返回前端。
 *
 * @author xingyun
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        // 业务校验失败属于客户端可修正的错误，用 400 而非 500，便于监控区分真实故障
        this.code = Result.CODE_BAD_REQUEST;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
