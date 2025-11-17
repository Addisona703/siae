package com.hngy.siae.auth.service.oauth;

import com.hngy.siae.auth.config.OAuthProperties;
import com.hngy.siae.auth.util.OAuthHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * QQ OAuth2登录服务
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QQOAuthService {
    
    private final OAuthProperties oAuthProperties;
    private final OAuthHttpClient httpClient;
    
    private static final String AUTHORIZE_URL = "https://graph.qq.com/oauth2.0/authorize";
    private static final String ACCESS_TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    private static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
    private static final String USER_INFO_URL = "https://graph.qq.com/user/get_user_info";
    
    /**
     * 生成QQ授权URL
     * 
     * @param state 状态参数
     * @return 授权URL
     */
    public String generateAuthUrl(String state) {
        try {
            OAuthProperties.QQConfig config = oAuthProperties.getQq();
            if (config == null || config.getAppId() == null) {
                throw new IllegalStateException("QQ OAuth配置未设置");
            }
            
            String authUrl = UriComponentsBuilder.fromHttpUrl(AUTHORIZE_URL)
                    .queryParam("client_id", config.getAppId())
                    .queryParam("redirect_uri", config.getRedirectUri())
                    .queryParam("response_type", "code")
                    .queryParam("state", state)
                    .toUriString();
            
            log.info("生成QQ授权URL成功: state={}", state);
            return authUrl;
            
        } catch (Exception e) {
            log.error("生成QQ授权URL失败: state={}, error={}", state, e.getMessage(), e);
            throw new RuntimeException("生成QQ授权URL失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用code换取access_token
     * 
     * @param code 授权码
     * @return access_token
     */
    public String getAccessToken(String code) {
        try {
            OAuthProperties.QQConfig config = oAuthProperties.getQq();
            
            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "authorization_code");
            params.put("client_id", config.getAppId());
            params.put("client_secret", config.getAppKey());
            params.put("code", code);
            params.put("redirect_uri", config.getRedirectUri());
            
            log.debug("请求QQ access_token: code={}", code);
            
            String response = httpClient.get(ACCESS_TOKEN_URL, params, null);
            
            // QQ返回格式: access_token=xxx&expires_in=7776000&refresh_token=xxx
            String accessToken = extractParam(response, "access_token");
            
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("获取QQ access_token失败: response={}", response);
                throw new RuntimeException("获取QQ access_token失败");
            }
            
            log.info("获取QQ access_token成功");
            return accessToken;
            
        } catch (Exception e) {
            log.error("获取QQ access_token失败: code={}, error={}", code, e.getMessage(), e);
            throw new RuntimeException("获取QQ access_token失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取QQ用户的openid
     * 
     * @param accessToken 访问令牌
     * @return openid
     */
    public String getOpenId(String accessToken) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("access_token", accessToken);
            
            log.debug("请求QQ openid");
            
            String response = httpClient.get(OPENID_URL, params, null);
            
            // QQ返回格式: callback( {"client_id":"xxx","openid":"xxx"} );
            // 需要提取JSON部分
            String json = extractJsonFromCallback(response);
            Map<String, Object> data = httpClient.parseJson(json);
            
            String openId = (String) data.get("openid");
            
            if (openId == null || openId.isEmpty()) {
                log.error("获取QQ openid失败: response={}", response);
                throw new RuntimeException("获取QQ openid失败");
            }
            
            log.info("获取QQ openid成功: openid={}", openId);
            return openId;
            
        } catch (Exception e) {
            log.error("获取QQ openid失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取QQ openid失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取QQ用户信息
     * 
     * @param accessToken 访问令牌
     * @param openId 用户openid
     * @return 用户信息Map，包含nickname和avatar
     */
    public Map<String, Object> getUserInfo(String accessToken, String openId) {
        try {
            OAuthProperties.QQConfig config = oAuthProperties.getQq();
            
            Map<String, String> params = new HashMap<>();
            params.put("access_token", accessToken);
            params.put("oauth_consumer_key", config.getAppId());
            params.put("openid", openId);
            
            log.debug("请求QQ用户信息: openid={}", openId);
            
            String response = httpClient.get(USER_INFO_URL, params, null);
            Map<String, Object> data = httpClient.parseJson(response);
            
            // 检查返回码
            Integer ret = (Integer) data.get("ret");
            if (ret == null || ret != 0) {
                String msg = (String) data.get("msg");
                log.error("获取QQ用户信息失败: ret={}, msg={}", ret, msg);
                throw new RuntimeException("获取QQ用户信息失败: " + msg);
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("nickname", data.get("nickname"));
            userInfo.put("avatar", data.get("figureurl_qq_2")); // 100x100头像
            userInfo.put("openid", openId);
            
            log.info("获取QQ用户信息成功: nickname={}", data.get("nickname"));
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取QQ用户信息失败: openid={}, error={}", openId, e.getMessage(), e);
            throw new RuntimeException("获取QQ用户信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从URL参数格式字符串中提取参数值
     * 
     * @param response 响应字符串
     * @param paramName 参数名
     * @return 参数值
     */
    private String extractParam(String response, String paramName) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        String[] pairs = response.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }
    
    /**
     * 从callback格式响应中提取JSON
     * 
     * @param response callback格式响应
     * @return JSON字符串
     */
    private String extractJsonFromCallback(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }
        
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}");
        
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        
        return "{}";
    }
}
