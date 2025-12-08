package com.hngy.siae.feign.decoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.Result;
import feign.FeignException;
import feign.Response;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Feign Result 解包解码器
 * <p>
 * 用于将 Provider 服务返回的 Result&lt;T&gt; 对象自动解包，提取其中的 data 字段。
 * 这样 Feign Client 接口可以直接返回 T 类型，而不需要返回 Result&lt;T&gt;。
 * <p>
 * 使用方式：
 * <pre>
 * &#64;Configuration
 * public class FeignConfig {
 *     &#64;Bean
 *     public Decoder feignDecoder(ObjectFactory&lt;HttpMessageConverters&gt; messageConverters) {
 *         return new ResultUnwrapDecoder(new SpringDecoder(messageConverters));
 *     }
 * }
 * </pre>
 *
 * @author SIAE开发团队
 */
@Slf4j
public class ResultUnwrapDecoder implements Decoder {
    
    private final Decoder delegate;
    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数
     *
     * @param delegate 委托的解码器，通常是 SpringDecoder
     */
    public ResultUnwrapDecoder(Decoder delegate) {
        this.delegate = delegate;
        this.objectMapper = createObjectMapper();
    }
    
    /**
     * 创建配置好的 ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册自定义的 Java 8 时间模块，支持 "yyyy-MM-dd HH:mm:ss" 格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addDeserializer(LocalDateTime.class, 
                new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule.addSerializer(LocalDateTime.class, 
                new LocalDateTimeSerializer(dateTimeFormatter));
        mapper.registerModule(javaTimeModule);
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 忽略未知属性
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }
    
    /**
     * 构造函数（支持自定义 ObjectMapper）
     *
     * @param delegate     委托的解码器
     * @param objectMapper 自定义的 ObjectMapper
     */
    public ResultUnwrapDecoder(Decoder delegate, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
        // 如果返回类型是 void 或 Response，直接使用委托解码器
        if (type == void.class || type == Response.class) {
            return delegate.decode(response, type);
        }
        
        // 如果响应状态码不是 2xx，让 ErrorDecoder 处理
        if (response.status() < 200 || response.status() >= 300) {
            return delegate.decode(response, type);
        }
        
        try {
            // 读取响应体
            String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
            
            if (log.isDebugEnabled()) {
                log.debug("Feign 响应体: {}", bodyStr);
            }
            
            // 构造 Result<T> 的 JavaType
            JavaType resultType = objectMapper.getTypeFactory()
                .constructParametricType(Result.class, objectMapper.constructType(type));
            
            // 将响应体反序列化为 Result<T>
            Result<?> result = objectMapper.readValue(bodyStr, resultType);
            
            // 检查业务状态码
            if (result.getCode() == 200) {
                // 成功：返回 data 字段
                return result.getData();
            } else {
                // 业务失败：抛出 ServiceException
                log.warn("Feign 调用业务失败: code={}, message={}", result.getCode(), result.getMessage());
                throw new ServiceException(result.getCode(), result.getMessage());
            }
            
        } catch (ServiceException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("Failed to unwrap Result object", e);
            // 如果解包失败，尝试使用委托解码器直接解码
            // 注意：这里需要重新创建 Response，因为 body 已经被读取
            return delegate.decode(response, type);
        }
    }
}
