//package com.hngy.siae.web.exception;
//
//
//import com.hngy.siae.core.exception.ServiceException;
//import com.hngy.siae.core.result.Result;
//import com.hngy.siae.core.result.CommonResultCodeEnum;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
//import org.springframework.validation.BindException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.stream.Collectors;
//
//@Slf4j
//@RestControllerAdvice
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
//public class GlobalExceptionHandler {
//
//    /**
//     * 处理自定义业务异常
//     */
//    @ExceptionHandler(ServiceException.class)
//    public Result<Void> handleBizException(ServiceException ex) {
//        log.warn("业务异常：{}", ex.getMessage());
//        return Result.error(ex.getCode(), ex.getMessage());
//    }
//
//    /**
//     * 处理参数校验异常 (@RequestBody 参数校验失败)
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
//        String message = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(field -> field.getField() + ": " + field.getDefaultMessage())
//                .collect(Collectors.joining("; "));
//        log.warn("参数验证失败：{}", message);
//        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), message);
//    }
//
//    /**
//     * 处理参数校验异常 (@ModelAttribute 参数校验失败)
//     */
//    @ExceptionHandler(BindException.class)
//    public Result<Void> handleBindException(BindException ex) {
//        String message = ex.getBindingResult()
//                .getAllErrors()
//                .stream()
//                .map(error -> (error instanceof FieldError)
//                        ? ((FieldError) error).getField() + ": " + error.getDefaultMessage()
//                        : error.getDefaultMessage())
//                .collect(Collectors.joining("; "));
//        log.warn("参数绑定失败：{}", message);
//        return Result.error(CommonResultCodeEnum.VALIDATE_FAILED.getCode(), message);
//    }
//
//    /**
//     * 捕获所有未处理异常
//     */
//    @ExceptionHandler(Exception.class)
//    public Result<Void> handleGlobalException(Exception ex, HttpServletRequest request) {
//        log.error("系统异常：{}，路径：{}", ex.getMessage(), request.getRequestURI(), ex);
//        return Result.error(CommonResultCodeEnum.ERROR.getCode(), "系统内部错误，请联系管理员！");
//    }
//}
