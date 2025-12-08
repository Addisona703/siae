package com.hngy.siae.feign.decoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.exception.BusinessException;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.Result;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * Feign Result 错误解码器
 * <p>
 * 用于将 Provider 服务返回的错误响应转换为业务异常。
 * 支持解析 Result 对象中的错误信息，并转换为对应的异常类型。
 * <p>
 * 使用方式：
 * <pre>
 * &#64;Configuration
 * public class FeignConfig {
 *     &#64;Bean
 *     public ErrorDecoder errorDecoder() {
 *         return new ResultErrorDecoder();
 *     }
 * }
 * </pre>
 *
 * @author SIAE开发团队
 */
@Slf4j
public class ResultErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultDecoder = new Default();
    private final ObjectMapper objectMapper;
    
    /**
     * 默认构造函数
     */
    public ResultErrorDecoder() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 构造函数（支持自定义 ObjectMapper）
     *
     * @param objectMapper 自定义的 ObjectMapper
     */
    public ResultErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 读取响应体
            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                Result<?> result = objectMapper.readValue(inputStream, Result.class);
                
                // 如果是业务异常（code != 200），转换为 BusinessException
                if (result.getCode() != 200) {
                    log.error("Feign 调用业务失败: method={}, code={}, message={}", 
                        methodKey, result.getCode(), result.getMessage());
                    return new BusinessException(result.getCode(), result.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("解析 Feign 错误响应失败: method={}", methodKey, e);
        }
        
        // 对于 5xx 错误，返回服务异常
        if (response.status() >= 500) {
            log.error("Feign 调用服务错误: method={}, status={}", methodKey, response.status());
            return new ServiceException("服务暂时不可用，请稍后重试");
        }
        
        // 其他情况使用默认解码器
        return defaultDecoder.decode(methodKey, response);
    }
}
