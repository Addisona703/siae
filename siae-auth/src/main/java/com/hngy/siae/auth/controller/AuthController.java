package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.dto.LoginRequest;
import com.hngy.siae.auth.dto.LoginResponse;
import com.hngy.siae.auth.dto.TokenRefreshRequest;
import com.hngy.siae.auth.dto.TokenRefreshResponse;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.auth.util.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 
 * @author KEYKB
 */
@Tag(name = "认证接口", description = "登录、登出、令牌刷新等认证相关接口")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 构造函数
     *
     * @param authService 认证服务
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * 用户登录
     *
     * @param request HTTP请求
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "用户名密码登录接口")
    @PostMapping("/login")
    public ApiResult<LoginResponse> login(HttpServletRequest request, @Valid @RequestBody LoginRequest loginRequest) {
        // 获取客户端信息
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);
        
        // 执行登录
        LoginResponse response = authService.login(loginRequest, clientIp, browser, os);
        return ApiResult.success(response);
    }
    
    /**
     * 刷新令牌
     *
     * @param refreshRequest 刷新令牌请求
     * @return 刷新令牌响应
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/token/refresh")
    public ApiResult<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenRefreshResponse response = authService.refreshToken(refreshRequest);
        return ApiResult.success(response);
    }
    
    /**
     * 退出登录
     *
     * @param request HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "退出登录", description = "使当前令牌失效")
    @PostMapping("/logout")
    public ApiResult<Boolean> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        authService.logout(token);
        return ApiResult.success(true);
    }
} 