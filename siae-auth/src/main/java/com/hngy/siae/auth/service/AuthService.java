package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.LoginRequest;
import com.hngy.siae.auth.dto.LoginResponse;
import com.hngy.siae.auth.dto.TokenRefreshRequest;
import com.hngy.siae.auth.dto.TokenRefreshResponse;

/**
 * 认证服务接口
 * 
 * @author KEYKB
 */
public interface AuthService {
    
    /**
     * 登录
     *
     * @param request  登录请求
     * @param clientIp 客户端IP
     * @param browser  浏览器
     * @param os       操作系统
     * @return 登录响应
     */
    LoginResponse login(LoginRequest request, String clientIp, String browser, String os);
    
    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 刷新令牌响应
     */
    TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    
    /**
     * 退出登录
     *
     * @param token 访问令牌
     */
    void logout(String token);
} 