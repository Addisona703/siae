package com.hngy.siae.auth.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth HTTP请求工具类
 * 用于调用第三方OAuth API
 * 
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthHttpClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 发送GET请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public String get(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            // 构建URL和参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            if (params != null && !params.isEmpty()) {
                params.forEach(builder::queryParam);
            }
            String finalUrl = builder.toUriString();
            
            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(httpHeaders::add);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            
            log.debug("发送GET请求: url={}", finalUrl);
            
            ResponseEntity<String> response = restTemplate.exchange(
                finalUrl,
                HttpMethod.GET,
                entity,
                String.class
            );
            
            String body = response.getBody();
            log.debug("GET请求响应: status={}, body={}", response.getStatusCode(), body);
            
            return body;
            
        } catch (RestClientException e) {
            log.error("GET请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP GET请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 发送POST请求
     * 
     * @param url 请求URL
     * @param params 请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public String post(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            // 构建请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(httpHeaders::add);
            }
            
            // 构建请求体
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            if (params != null && !params.isEmpty()) {
                params.forEach(body::add);
            }
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, httpHeaders);
            
            log.debug("发送POST请求: url={}, params={}", url, params);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            String responseBody = response.getBody();
            log.debug("POST请求响应: status={}, body={}", response.getStatusCode(), responseBody);
            
            return responseBody;
            
        } catch (RestClientException e) {
            log.error("POST请求失败: url={}, error={}", url, e.getMessage(), e);
            throw new RuntimeException("HTTP POST请求失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析JSON字符串为Map
     * 
     * @param json JSON字符串
     * @return Map对象
     */
    public Map<String, Object> parseJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            log.warn("JSON字符串为空");
            return new HashMap<>();
        }
        
        try {
            Map<String, Object> result = objectMapper.readValue(
                json,
                    new TypeReference<>() {
                    }
            );
            log.debug("JSON解析成功: keys={}", result.keySet());
            return result;
            
        } catch (Exception e) {
            log.error("JSON解析失败: json={}, error={}", json, e.getMessage(), e);
            throw new RuntimeException("JSON解析失败: " + e.getMessage(), e);
        }
    }
}
