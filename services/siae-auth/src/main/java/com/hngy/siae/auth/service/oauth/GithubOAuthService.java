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
 * GitHub OAuth2登录服务
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuthService {
    
    private final OAuthProperties oAuthProperties;
    private final OAuthHttpClient httpClient;
    
    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String USER_INFO_URL = "https://api.github.com/user";
    
    /**
     * 生成GitHub授权URL
     * 
     * @param state 状态参数
     * @return 授权URL
     */
    public String generateAuthUrl(String state) {
        try {
            OAuthProperties.GithubConfig config = oAuthProperties.getGithub();
            if (config == null || config.getClientId() == null) {
                throw new IllegalStateException("GitHub OAuth配置未设置");
            }
            
            String authUrl = UriComponentsBuilder.fromHttpUrl(AUTHORIZE_URL)
                    .queryParam("client_id", config.getClientId())
                    .queryParam("redirect_uri", config.getRedirectUri())
                    .queryParam("scope", "user:email")
                    .queryParam("state", state)
                    .toUriString();
            
            log.info("生成GitHub授权URL成功: state={}", state);
            return authUrl;
            
        } catch (Exception e) {
            log.error("生成GitHub授权URL失败: state={}, error={}", state, e.getMessage(), e);
            throw new RuntimeException("生成GitHub授权URL失败: " + e.getMessage(), e);
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
            OAuthProperties.GithubConfig config = oAuthProperties.getGithub();
            
            Map<String, String> params = new HashMap<>();
            params.put("client_id", config.getClientId());
            params.put("client_secret", config.getClientSecret());
            params.put("code", code);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");
            
            log.debug("请求GitHub access_token: code={}", code);
            
            String response = httpClient.post(ACCESS_TOKEN_URL, params, headers);
            Map<String, Object> data = httpClient.parseJson(response);
            
            // 检查错误
            if (data.containsKey("error")) {
                String error = (String) data.get("error");
                String errorDescription = (String) data.get("error_description");
                log.error("获取GitHub access_token失败: error={}, description={}", error, errorDescription);
                throw new RuntimeException("获取GitHub access_token失败: " + errorDescription);
            }
            
            String accessToken = (String) data.get("access_token");
            
            if (accessToken == null || accessToken.isEmpty()) {
                log.error("获取GitHub access_token失败: response={}", response);
                throw new RuntimeException("获取GitHub access_token失败");
            }
            
            log.info("获取GitHub access_token成功");
            return accessToken;
            
        } catch (Exception e) {
            log.error("获取GitHub access_token失败: code={}, error={}", code, e.getMessage(), e);
            throw new RuntimeException("获取GitHub access_token失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取GitHub用户信息
     * 
     * @param accessToken 访问令牌
     * @return 用户信息Map，包含id、login、avatar_url
     */
    public Map<String, Object> getUserInfo(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/json");
            
            log.debug("请求GitHub用户信息");
            
            String response = httpClient.get(USER_INFO_URL, null, headers);
            Map<String, Object> data = httpClient.parseJson(response);
            
            // 检查是否有错误信息
            if (data.containsKey("message")) {
                String message = (String) data.get("message");
                log.error("获取GitHub用户信息失败: message={}", message);
                throw new RuntimeException("获取GitHub用户信息失败: " + message);
            }
            
            // GitHub返回的id是Integer类型，需要转换为String
            Object idObj = data.get("id");
            String userId = idObj != null ? String.valueOf(idObj) : null;
            
            if (userId == null) {
                log.error("获取GitHub用户信息失败: 缺少id字段, response={}", response);
                throw new RuntimeException("获取GitHub用户信息失败: 缺少id字段");
            }
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", userId);
            userInfo.put("nickname", data.get("login")); // GitHub用户名作为昵称
            userInfo.put("avatar", data.get("avatar_url"));
            userInfo.put("name", data.get("name")); // 真实姓名（可能为空）
            userInfo.put("email", data.get("email")); // 邮箱（可能为空）
            
            log.info("获取GitHub用户信息成功: login={}, id={}", data.get("login"), userId);
            return userInfo;
            
        } catch (Exception e) {
            log.error("获取GitHub用户信息失败: error={}", e.getMessage(), e);
            throw new RuntimeException("获取GitHub用户信息失败: " + e.getMessage(), e);
        }
    }
}
