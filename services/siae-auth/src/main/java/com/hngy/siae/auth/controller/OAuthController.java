package com.hngy.siae.auth.controller;

import com.hngy.siae.auth.dto.request.UnbindRequest;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.OAuthAccountVO;
import com.hngy.siae.auth.service.OAuthService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.web.utils.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth第三方登录控制器
 * 
 * @author SIAE
 */
@Tag(name = "OAuth第三方登录", description = "第三方登录相关API")
@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthService oauthService;

    /**
     * 发起第三方登录
     *
     * @param provider 第三方平台标识（qq/wx/github）
     * @return 授权URL
     */
    @Operation(summary = "发起第三方登录", description = "生成第三方授权URL")
    @GetMapping("/login")
    public Result<Map<String, String>> login(@RequestParam String provider) {
        String authUrl = oauthService.generateAuthUrl(provider);
        Map<String, String> result = new HashMap<>();
        result.put("authUrl", authUrl);
        return Result.success(result);
    }

    /**
     * 第三方授权回调处理
     *
     * @param provider 第三方平台标识
     * @param code 授权码
     * @param state 状态参数
     * @param request HTTP请求
     * @return 登录结果（token和用户信息）
     */
    @Operation(summary = "第三方授权回调", description = "处理第三方平台的授权回调")
    @GetMapping("/callback/{provider}")
    public Result<LoginVO> callback(@PathVariable String provider,
                                    @RequestParam String code,
                                    @RequestParam String state,
                                    HttpServletRequest request) {
        String clientIp = WebUtils.getClientIp(request);
        String browser = WebUtils.getBrowser(request);
        String os = WebUtils.getOs(request);

        LoginVO loginResponse = oauthService.handleCallback(provider, code, state, clientIp, browser, os);
        return Result.success(loginResponse);
    }

    /**
     * 绑定第三方账号 - 生成授权URL
     *
     * @param provider 第三方平台标识（qq/wx/github）
     * @return 授权URL
     */
    @Operation(summary = "绑定第三方账号", description = "已登录用户绑定第三方账号，返回授权URL")
    @PostMapping("/bind")
    public Result<Map<String, String>> bind(@RequestParam String provider) {
        // 生成授权URL（用户认证已由过滤器完成）
        String authUrl = oauthService.generateAuthUrl(provider);
        Map<String, String> result = new HashMap<>();
        result.put("authUrl", authUrl);
        return Result.success(result);
    }

    /**
     * 绑定第三方账号 - 处理回调
     *
     * @param provider 第三方平台标识
     * @param code 授权码
     * @param state 状态参数
     * @return 操作结果
     */
    @Operation(summary = "绑定回调处理", description = "处理第三方账号绑定的授权回调")
    @GetMapping("/bind/callback/{provider}")
    public Result<Boolean> bindCallback(@PathVariable String provider,
                                        @RequestParam String code,
                                        @RequestParam String state) {
        Long userId = getCurrentUserId();
        oauthService.bindAccount(userId, provider, code, state);
        return Result.success(true);
    }

    /**
     * 解绑第三方账号
     *
     * @param unbindRequest 解绑请求
     * @return 操作结果
     */
    @Operation(summary = "解绑第三方账号", description = "解除第三方账号绑定")
    @PostMapping("/unbind")
    public Result<Boolean> unbind(@Valid @RequestBody UnbindRequest unbindRequest) {
        Long userId = getCurrentUserId();
        oauthService.unbindAccount(userId, unbindRequest.getProvider());
        return Result.success(true);
    }

    /**
     * 查询已绑定的第三方账号列表
     *
     * @return 绑定账号列表
     */
    @Operation(summary = "查询已绑定账号", description = "获取用户绑定的第三方账号列表")
    @GetMapping("/accounts")
    public Result<List<OAuthAccountVO>> getAccounts() {
        Long userId = getCurrentUserId();
        List<OAuthAccountVO> accounts = oauthService.getUserAccounts(userId);
        return Result.success(accounts);
    }

    /**
     * 从Spring Security上下文中获取当前用户ID
     * ServiceAuthenticationFilter已经完成认证并将用户信息设置到SecurityContext中
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 过滤器已保证认证通过，直接获取userId
        Object details = authentication.getDetails();
        AssertUtils.isTrue(details instanceof Long, "无法获取用户ID");
        
        return (Long) details;
    }
}
