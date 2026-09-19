package com.sunyongjie.blog.common;

import lombok.Getter;

/**
 * 业务异常。由 GlobalExceptionHandler 统一转成 {@link Result}，
 * 不需要 try-catch，也不会被兜底分支记成 500。
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    public BizException(String message) {
        this(ResultCode.BAD_REQUEST, message);
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
