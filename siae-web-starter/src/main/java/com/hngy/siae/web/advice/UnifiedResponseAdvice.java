package com.hngy.siae.web.advice;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.annotation.UnifiedResponse;
import com.hngy.siae.web.properties.WebProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;

/**
 * 统一响应体包装器
 * 支持配置化的响应包装和路径排除
 * 
 * @author SIAE开发团队
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "siae.web.response", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class UnifiedResponseAdvice implements ResponseBodyAdvice<Object> {

    private final WebProperties webProperties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 判断是否需要应用此建议
     * 
     * @param returnType    返回类型
     * @param converterType 转换器类型
     * @return 是否应用建议
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查方法或类是否有 @UnifiedResponse 注解
        boolean hasAnnotation = returnType.hasMethodAnnotation(UnifiedResponse.class) ||
                               returnType.getContainingClass().isAnnotationPresent(UnifiedResponse.class);
        
        if (!hasAnnotation) {
            return false;
        }

        // 检查是否在配置的基础包路径内
        String className = returnType.getContainingClass().getName();

        return Arrays.stream(webProperties.getResponse().getBasePackages())
                .anyMatch(className::startsWith);
    }

    /**
     * 在响应体写入前进行拦截处理
     * 
     * @param body                  响应体
     * @param returnType            返回类型
     * @param selectedContentType   选择的内容类型
     * @param selectedConverterType 选择的转换器类型
     * @param request               当前请求
     * @param response              当前响应
     * @return 修改后的响应体
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        
        // 检查是否为排除路径
        String path = request.getURI().getPath();
        boolean isExcluded = Arrays.stream(webProperties.getResponse().getExcludePatterns())
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        
        if (isExcluded) {
            log.debug("Path {} is excluded from unified response wrapping", path);
            return body;
        }

        // 如果响应体已经是 Result 对象，不再包装
        if (body instanceof Result) {
            return body;
        }

        // 如果方法返回 void，包装成功的空响应
        if (returnType.getParameterType().equals(void.class)) {
            return Result.success();
        }

        // 包装原始响应体
        return Result.success(body);
    }
}
