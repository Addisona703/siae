package com.hngy.siae.web.feign;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.Result;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 自定义 Feign 响应解码器
 * 自动拆包 Result<T> 并处理业务异常
 */
@Slf4j
//@Component
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class FeignResultDecoder implements Decoder {

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot 会自动注入配置好的 ObjectMapper

    public FeignResultDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        // 1. 读取响应体
        String bodyStr = feign.Util.toString(response.body().asReader(feign.Util.UTF_8));
        log.debug("Feign 响应体: {}", bodyStr);

        // 先打印响应，确认格式
//        System.out.println("Feign响应体: " + bodyStr);

        // 2. 构造 Result<T> 的 JavaType，这是正确反序列化泛型的关键
        JavaType resultType = TypeFactory.defaultInstance().constructParametricType(Result.class, TypeFactory.defaultInstance().constructType(type));
        
        // 3. 将响应体反序列化为 Result<T>
        Result<?> result = objectMapper.readValue(bodyStr, resultType);

        // 4. 检查业务码
        // 您可以根据您的 ResultCodeEnum 定义来判断成功状态
        if (result.getCode() == 200) {
            // 如果成功，直接返回 data 部分的业务对象
            return result.getData();
        } else {
            // 如果是业务失败，抛出自定义的 ServiceException
            // 这样上层业务代码可以通过 try-catch 来捕获业务异常
            throw new ServiceException(result.getMessage());
        }
    }
}
