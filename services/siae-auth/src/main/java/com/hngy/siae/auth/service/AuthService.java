package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.dto.request.RegisterDTO;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.request.TokenRefreshDTO;
import com.hngy.siae.auth.dto.response.RegisterVO;
import com.hngy.siae.auth.dto.response.TokenRefreshVO;
import com.hngy.siae.auth.entity.UserAuth;
import com.hngy.siae.auth.dto.response.CurrentUserVO;

/**
 * 认证服务接口
 *
 * @author KEYKB
 */
public interface AuthService extends IService<UserAuth> {
    
    /**
     * 登录
     *
     * @param loginDTO  登录请求
     * @param clientIp 客户端IP
     * @param browser  浏览器
     * @param os       操作系统
     * @return 登录响应
     */
    LoginVO login(LoginDTO loginDTO, String clientIp, String browser, String os);
    
    /**
     * 注册
     *
     * @param request  注册请求
     * @param clientIp 客户端IP
     * @param browser  浏览器
     * @param os       操作系统
     * @return 注册响应
     */
    RegisterVO register(RegisterDTO request, String clientIp, String browser, String os);
    
    /**
     * 刷新令牌
     *
     * @param request 刷新令牌请求
     * @return 刷新令牌响应
     */
    TokenRefreshVO refreshToken(TokenRefreshDTO request);
    
    /**
     * 退出登录
     *
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 获取当前登录用户信息
     *
     * @param authorizationHeader 请求头中的Authorization字段
     * @return 当前用户视图对象
     */
    CurrentUserVO getCurrentUser(String authorizationHeader);
}
