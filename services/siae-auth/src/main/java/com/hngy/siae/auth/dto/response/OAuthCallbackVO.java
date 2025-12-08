package com.hngy.siae.auth.dto.response;

import lombok.Data;

/**
 * OAuth回调响应VO
 * 用于区分已绑定用户（直接登录）和新用户（需要完善信息）
 * 
 * @author SIAE
 */
@Data
public class OAuthCallbackVO {
    
    /**
     * 是否需要完善信息（新用户为true）
     */
    private Boolean needRegister;
    
    /**
     * 临时令牌（新用户用于完善信息时验证身份）
     */
    private String tempToken;
    
    /**
     * 第三方平台标识
     */
    private String provider;
    
    /**
     * 第三方用户ID
     */
    private String providerUserId;
    
    /**
     * 第三方昵称（可作为默认用户名建议）
     */
    private String nickname;
    
    /**
     * 第三方头像URL
     */
    private String avatar;
    
    /**
     * 登录信息（已绑定用户直接返回）
     */
    private LoginVO loginInfo;
    
    /**
     * 创建需要注册的响应
     */
    public static OAuthCallbackVO needRegister(String tempToken, String provider, 
            String providerUserId, String nickname, String avatar) {
        OAuthCallbackVO vo = new OAuthCallbackVO();
        vo.setNeedRegister(true);
        vo.setTempToken(tempToken);
        vo.setProvider(provider);
        vo.setProviderUserId(providerUserId);
        vo.setNickname(nickname);
        vo.setAvatar(avatar);
        return vo;
    }
    
    /**
     * 创建直接登录的响应
     */
    public static OAuthCallbackVO directLogin(LoginVO loginInfo) {
        OAuthCallbackVO vo = new OAuthCallbackVO();
        vo.setNeedRegister(false);
        vo.setLoginInfo(loginInfo);
        return vo;
    }
}
