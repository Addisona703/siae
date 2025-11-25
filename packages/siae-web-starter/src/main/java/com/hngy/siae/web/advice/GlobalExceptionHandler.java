package com.hngy.siae.web.advice;

import com.hngy.siae.core.exception.BusinessException;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.web.properties.WebProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 支持配置化的异常处理和错误信息返回
 * 
 * @author SIAE开发团队
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "siae.web.exception", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final WebProperties webProperties;

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("业务异常：{}，路径：{}", ex.getMessage(), request.getRequestURI());
        }
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理服务异常（兼容旧版本）
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleServiceException(ServiceException ex, HttpServletRequest request) {
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("服务异常：{}，路径：{}", ex.getMessage(), request.getRequestURI());
        }
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常 (@RequestBody 参数校验失败)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("参数验证失败：{}，路径：{}", message, request.getRequestURI());
        }
        
        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 处理参数校验异常 (@ModelAttribute 参数校验失败)
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> (error instanceof FieldError)
                        ? ((FieldError) error).getField() + ": " + error.getDefaultMessage()
                        : error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("参数绑定失败：{}，路径：{}", message, request.getRequestURI());
        }
        
        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 处理约束违反异常 (@Validated 参数校验失败)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("约束违反：{}，路径：{}", message, request.getRequestURI());
        }
        
        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        if (webProperties.getException().isPrintStackTrace()) {
            log.warn("非法参数：{}，路径：{}", ex.getMessage(), request.getRequestURI());
        }
        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), ex.getMessage());
    }

    /**
     * 捕获所有未处理异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("系统异常：{}，路径：{}", ex.getMessage(), request.getRequestURI(), ex);
        
        String message = "系统内部错误，请联系管理员！";
        if (webProperties.getException().isIncludeStackTrace()) {
            message = ex.getMessage();
        }
        
        return Result.error(CommonResultCodeEnum.ERROR.getCode(), message);
    }
}
