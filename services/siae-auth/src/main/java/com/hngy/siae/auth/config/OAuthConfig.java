package com.hngy.siae.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth配置类
 * 
 * @author KEYKB
 */
@Configuration
public class OAuthConfig {
    
    /**
     * 配置用于OAuth HTTP请求的RestTemplate
     * 
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时5秒
        factory.setReadTimeout(10000);   // 读取超时10秒
        return new RestTemplate(factory);
    }
}
