package com.hngy.siae.auth.filter;

import com.hngy.siae.core.utils.JwtUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceCallFeignInterceptor implements RequestInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public void apply(RequestTemplate template) {
        // 这里动态生成一个服务调用token，比如给auth服务id生成token
        String serviceCallToken = jwtUtils.createServiceCallToken();
        System.out.println("正在生成调用user服务的token...");
        template.header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceCallToken);
    }
}
