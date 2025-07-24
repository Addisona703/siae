package com.hngy.siae.user.dto.request;

import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户第三方认证数据传输对象
 * 
 * @author KEYKB
 */
@Data
public class UserThirdPartyAuthDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    @NotNull(message = "ID不能为空", groups = {UpdateGroup.class})
    private Long id;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Long userId;
    
    /**
     * 第三方平台类型：1微信，2QQ，3微博，4GitHub
     */
    @NotNull(message = "第三方平台类型不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private Integer type;
    
    /**
     * 第三方平台用户唯一标识
     */
    @NotBlank(message = "第三方平台用户唯一标识不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    private String openId;
    
    /**
     * 第三方平台访问令牌
     */
    private String accessToken;
    
    /**
     * 第三方平台刷新令牌
     */
    private String refreshToken;
    
    /**
     * 第三方平台用户昵称
     */
    private String nickname;
    
    /**
     * 第三方平台用户头像
     */
    private String avatar;
    
    /**
     * 令牌过期时间
     */
    private Long expiresIn;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 