package com.hngy.siae.auth.controller;

import cn.hutool.core.util.StrUtil;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.dto.request.RegisterDTO;
import com.hngy.siae.auth.dto.request.TokenRefreshDTO;
import com.hngy.siae.auth.dto.response.CurrentUserVO;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.RegisterVO;
import com.hngy.siae.auth.dto.response.TokenRefreshVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.web.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 * 
 * @author KEYKB
 */
@Tag(name = "认证管理", description = "认证相关API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
    private static final int DEFAULT_REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60; // 30 days

    /**
     * 用户登录
     *
     * @param request  HTTP请求
     * @param loginDTO 登录请求DTO
     * @return 登录结果
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(HttpServletRequest request,
                                 HttpServletResponse servletResponse,
                                 @Valid @RequestBody LoginDTO loginDTO) {
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);

        LoginVO loginResponse = authService.login(loginDTO, clientIp, browser, os);
        writeTokenCookies(loginResponse.getAccessToken(),
                loginResponse.getExpiresIn(),
                loginResponse.getRefreshToken(),
                servletResponse);
        return Result.success(loginResponse);
    }

    /**
     * 用户注册
     * 
     * @param request HTTP请求
     * @param registerDTO 注册请求DTO
     * @return 注册结果
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<RegisterVO> register(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @Valid @RequestBody RegisterDTO registerDTO) {
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);
        
        RegisterVO registerResponse = authService.register(registerDTO, clientIp, browser, os);
        writeTokenCookies(registerResponse.getAccessToken(),
                registerResponse.getExpiresIn(),
                registerResponse.getRefreshToken(),
                response);
        return Result.success(registerResponse);
    }

    /**
     * 刷新访问令牌
     *
     * @param TokenRefreshDTO 刷新令牌请求
     * @return 新的访问令牌
     */
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<TokenRefreshVO> refreshToken(HttpServletResponse servletResponse,
                                               @Valid @RequestBody TokenRefreshDTO TokenRefreshDTO) {
        TokenRefreshVO refreshResponse = authService.refreshToken(TokenRefreshDTO);
        writeTokenCookies(refreshResponse.getAccessToken(),
                refreshResponse.getExpiresIn(),
                refreshResponse.getRefreshToken(),
                servletResponse);
        return Result.success(refreshResponse);
    }

    /**
     * 用户登出
     *
     * @param request HTTP请求
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "使当前令牌失效")
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        // 清理 Spring Security 上下文
        SecurityContextHolder.clearContext();
        String token = resolveAccessToken(request);
        authService.logout(token);
        clearTokenCookie(response, ACCESS_TOKEN_COOKIE);
        clearTokenCookie(response, REFRESH_TOKEN_COOKIE);
        return Result.success(true);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求
     * @return 当前用户基础信息、角色与权限
     */
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基础资料、角色与权限列表")
    @GetMapping("/me")
    public Result<CurrentUserVO> getCurrentUser(HttpServletRequest request) {
        String token = resolveAccessToken(request);
        CurrentUserVO currentUser = authService.getCurrentUser(token != null ? "Bearer " + token : null);
        return Result.success(currentUser);
    }

    private void writeTokenCookies(String accessToken,
                                   Long accessTokenTtlSeconds,
                                   String refreshToken,
                                   HttpServletResponse response) {
        if (StrUtil.isNotBlank(accessToken) && accessTokenTtlSeconds != null) {
            Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
            accessCookie.setHttpOnly(true);
            accessCookie.setSecure(false);
            accessCookie.setPath("/");
            accessCookie.setMaxAge(accessTokenTtlSeconds.intValue());
            response.addCookie(accessCookie);
        }

        if (StrUtil.isNotBlank(refreshToken)) {
            Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(DEFAULT_REFRESH_TOKEN_MAX_AGE);
            response.addCookie(refreshCookie);
        }
    }

    private void clearTokenCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName()) &&
                    StrUtil.isNotBlank(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
