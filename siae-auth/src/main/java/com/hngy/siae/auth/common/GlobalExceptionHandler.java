package com.hngy.siae.auth.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 
 * @author KEYKB
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理认证异常
     */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ApiResult<Void> handleAuthenticationException(AuthenticationException e) {
        log.error("认证异常", e);
        return ApiResult.error(401, e.getMessage());
    }
    
    /**
     * 处理授权异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResult<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.error("授权异常", e);
        return ApiResult.error(403, "没有权限访问该资源");
    }
    
    /**
     * 处理参数校验异常 (JSON请求体)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResult.error(400, errorMessage);
    }
    
    /**
     * 处理参数校验异常 (表单提交)
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResult<Void> handleBindException(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResult.error(400, errorMessage);
    }
    
    /**
     * 处理业务异常
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusinessException(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理其他异常
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResult.error(500, "服务器内部错误");
    }
} 