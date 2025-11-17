package com.hngy.siae.auth.service;

import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.OAuthAccountVO;

import java.util.List;

/**
 * OAuth第三方登录服务接口
 * 
 * @author SIAE
 */
public interface OAuthService {
    
    /**
     * 生成第三方授权URL
     * 
     * @param provider 第三方平台标识（qq/wx/github）
     * @return 授权URL
     */
    String generateAuthUrl(String provider);
    
    /**
     * 处理第三方授权回调
     * 
     * @param provider 第三方平台标识
     * @param code 授权码
     * @param state 状态参数
     * @param clientIp 客户端IP
     * @param browser 浏览器信息
     * @param os 操作系统信息
     * @return 登录响应
     */
    LoginVO handleCallback(String provider, String code, String state, String clientIp, String browser, String os);
    
    /**
     * 绑定第三方账号
     * 
     * @param userId 用户ID
     * @param provider 第三方平台标识
     * @param code 授权码
     * @param state 状态参数
     */
    void bindAccount(Long userId, String provider, String code, String state);
    
    /**
     * 解绑第三方账号
     * 
     * @param userId 用户ID
     * @param provider 第三方平台标识
     */
    void unbindAccount(Long userId, String provider);
    
    /**
     * 查询用户绑定的第三方账号列表
     * 
     * @param userId 用户ID
     * @return 绑定账号列表
     */
    List<OAuthAccountVO> getUserAccounts(Long userId);
}
