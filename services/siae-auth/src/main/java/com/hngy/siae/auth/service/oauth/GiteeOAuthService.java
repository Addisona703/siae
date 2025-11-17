package com.hngy.siae.auth.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.auth.config.OAuthProperties;
import com.hngy.siae.auth.util.OAuthHttpClient;
import com.hngy.siae.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Gitee OAuth 服务实现
 * 
 * @author SIAE
 */
@Slf4j
@Service("giteeOAuthService")
@RequiredArgsConstructor
public class GiteeOAuthService {
    
    private final OAuthProperties oAuthProperties;
    private final OAuthHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Gitee OAuth 端点
    private static final String AUTH_URL = "https://gitee.com/oauth/authorize";
    private static final String TOKEN_URL = "https://gitee.com/oauth/token";
    private static final String USER_INFO_URL = "https://gitee.com/api/v5/user";

    public String generateAuthUrl(String state) {
        OAuthProperties.GiteeConfig config = oAuthProperties.getGitee();
        
        return AUTH_URL + "?client_id=" + config.getClientId()
                + "&redirect_uri=" + config.getRedirectUri()
                + "&response_type=code"
                + "&state=" + state;
    }

    public Map<String, Object> getUserInfo(String code) {
        try {
            // 1. 换取 access_token
            String accessToken = getAccessToken(code);
            
            // 2. 获取用户信息
            return fetchUserInfo(accessToken);
            
        } catch (Exception e) {
            log.error("Gitee OAuth 获取用户信息失败", e);
            throw new ServiceException("Gitee 登录失败: " + e.getMessage());
        }
    }
    
    /**
     * 换取 access_token
     */
    private String getAccessToken(String code) throws Exception {
        OAuthProperties.GiteeConfig config = oAuthProperties.getGitee();
        
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", config.getClientId());
        params.put("client_secret", config.getClientSecret());
        params.put("redirect_uri", config.getRedirectUri());
        
        String response = httpClient.post(TOKEN_URL, params, null);
        JsonNode jsonNode = objectMapper.readTree(response);
        
        if (jsonNode.has("error")) {
            throw new ServiceException("Gitee 获取 token 失败: " + jsonNode.get("error_description").asText());
        }
        
        return jsonNode.get("access_token").asText();
    }
    
    /**
     * 获取用户信息
     */
    private Map<String, Object> fetchUserInfo(String accessToken) throws Exception {
        String url = USER_INFO_URL + "?access_token=" + accessToken;
        String response = httpClient.get(url, null, null);
        JsonNode userNode = objectMapper.readTree(response);
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("provider_user_id", userNode.get("id").asText());
        userInfo.put("nickname", userNode.get("name").asText());
        userInfo.put("avatar", userNode.has("avatar_url") ? userNode.get("avatar_url").asText() : null);
        userInfo.put("access_token", accessToken);
        userInfo.put("raw_json", response);
        
        return userInfo;
    }
}
