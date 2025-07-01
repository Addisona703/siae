package com.hngy.siae.common.advice;

import com.hngy.siae.common.annotation.UnifiedResponse;
import com.hngy.siae.common.result.Result;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应体包装器
 */
@RestControllerAdvice(basePackages = "com.hngy.siae") // Only scan your project's base package
public class UnifiedResponseAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Determines if this advice should be applied.
     * It checks for the presence of the @UnifiedResponse annotation on the method or its class.
     *
     * @param returnType    the return type
     * @param converterType the selected converter type
     * @return true if the advice should be applied, false otherwise
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // Check if the method or the containing class has the @UnifiedResponse annotation.
        return returnType.hasMethodAnnotation(UnifiedResponse.class) ||
               returnType.getContainingClass().isAnnotationPresent(UnifiedResponse.class);
    }

    /**
     * Intercepts the response body before it's written to the response.
     *
     * @param body                  the body to be written
     * @param returnType            the return type of the com.hngy.siae.content.controller method
     * @param selectedContentType   the content type selected by the MessageConverter
     * @param selectedConverterType the converter type selected to write to the response
     * @param request               the current request
     * @param response              the current response
     * @return the modified body, which may be a new instance
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // If the response body is already a Result object, do not wrap it again.
        // This is useful for when the GlobalExceptionHandler handles an error.
        if (body instanceof Result) {
            return body;
        }

        // If the com.hngy.siae.content.controller method returns void (e.g., for a delete operation), wrap a successful empty response.
        if (returnType.getParameterType().equals(void.class)) {
            return Result.success();
        }

        // Wrap the original response body in a Result object.
        return Result.success(body);
    }
}
