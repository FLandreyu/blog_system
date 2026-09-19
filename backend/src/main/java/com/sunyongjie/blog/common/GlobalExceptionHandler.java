package com.sunyongjie.blog.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理：保证任何一条出口路径（包括异常路径）返回的都是 {@link Result} 结构，
 * 前端只需要判断 {@code code}。
 *
 * <p>HTTP 状态码与业务 code 保持一致，这样 curl 和 axios 拦截器都能直接按状态码分支。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：可预期，按 WARN 记录，不打印堆栈 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException e, HttpServletRequest request) {
        log.warn("业务异常 [{} {}] code={} message={}",
                request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return build(e.getCode(), e.getMessage());
    }

    /** @Valid 校验失败（@RequestBody） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e,
                                                         HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("；"));
        if (message.isEmpty()) {
            message = "参数校验失败";
        }
        log.warn("参数校验失败 [{} {}] {}", request.getMethod(), request.getRequestURI(), message);
        return build(ResultCode.BAD_REQUEST, message);
    }

    /** 方法参数上的约束校验失败（@Validated + @RequestParam 等） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException e,
                                                                  HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败 [{} {}] {}", request.getMethod(), request.getRequestURI(), message);
        return build(ResultCode.BAD_REQUEST, message.isEmpty() ? "参数校验失败" : message);
    }

    /** 请求体不是合法 JSON、缺少必填参数、参数类型对不上 —— 都算参数错误 */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(Exception e, HttpServletRequest request) {
        log.warn("请求参数不合法 [{} {}] {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return build(ResultCode.BAD_REQUEST, "请求参数不合法");
    }

    /** 静态资源 / 未知路径 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResource(NoResourceFoundException e,
                                                         HttpServletRequest request) {
        log.warn("资源不存在 [{} {}]", request.getMethod(), request.getRequestURI());
        return build(ResultCode.NOT_FOUND, "请求的资源不存在");
    }

    /** 兜底：未预期异常统一 500 + 友好提示，日志里打完整堆栈 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{} {}]", request.getMethod(), request.getRequestURI(), e);
        return build(ResultCode.ERROR, "服务器内部错误，请稍后重试");
    }

    private ResponseEntity<Result<Void>> build(int code, String message) {
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.BAD_REQUEST;
        }
        return ResponseEntity.status(status).body(Result.fail(code, message));
    }
}
