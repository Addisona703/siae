package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.ChangePasswordDTO;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.dto.request.RegisterDTO;
import com.hngy.siae.auth.dto.request.TokenRefreshDTO;
import com.hngy.siae.auth.dto.response.CurrentUserVO;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.RegisterVO;
import com.hngy.siae.auth.dto.response.TokenRefreshVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.security.utils.SecurityUtil;
import com.hngy.siae.web.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final SecurityUtil securityUtil;

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
                                 @Valid @RequestBody LoginDTO loginDTO) {
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);

        LoginVO loginResponse = authService.login(loginDTO, clientIp, browser, os);
        return Result.success("登录成功", loginResponse);
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
                                       @Valid @RequestBody RegisterDTO registerDTO) {
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);
        
        RegisterVO registerResponse = authService.register(registerDTO, clientIp, browser, os);
        return Result.success("注册成功", registerResponse);
    }

    /**
     * 刷新访问令牌
     *
     * @param TokenRefreshDTO 刷新令牌请求
     * @return 新的访问令牌
     */
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh-token")
    public Result<TokenRefreshVO> refreshToken(@Valid @RequestBody TokenRefreshDTO TokenRefreshDTO) {
        TokenRefreshVO refreshResponse = authService.refreshToken(TokenRefreshDTO);
        return Result.success(refreshResponse);
    }

    /**
     * 用户登出
     *
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "使当前令牌失效")
    @PostMapping("/logout")
    @SiaeAuthorize("isAuthenticated()")
    public Result<Boolean> logout() {
        Long userId = securityUtil.getCurrentUserId();
        authService.logout(userId);
        return Result.success("成功登出", true);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 当前用户基础信息、角色与权限
     */
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基础资料、角色与权限列表")
    @GetMapping("/me")
    @SiaeAuthorize("isAuthenticated()")
    public Result<CurrentUserVO> getCurrentUser() {
        Long userId = securityUtil.getCurrentUserId();
        CurrentUserVO currentUser = authService.getCurrentUser(userId);
        return Result.success(currentUser);
    }

    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码请求DTO
     * @return 修改结果
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PostMapping("/change-password")
    @SiaeAuthorize("isAuthenticated()")
    public Result<Boolean> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        Long userId = securityUtil.getCurrentUserId();
        authService.changePassword(userId, changePasswordDTO);
        return Result.success("密码修改成功", true);
    }
}
