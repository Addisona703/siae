package com.hngy.siae.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.auth.dto.request.ChangePasswordDTO;
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
     * @param userId 用户ID（由 Controller 从 Security 上下文获取）
     */
    void logout(Long userId);

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID（由 Controller 从 Security 上下文获取）
     * @return 当前用户视图对象
     */
    CurrentUserVO getCurrentUser(Long userId);

    /**
     * 修改密码
     *
     * @param userId 用户ID（由 Controller 从 Security 上下文获取）
     * @param changePasswordDTO 修改密码请求
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);
}
